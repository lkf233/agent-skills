alter table if exists conversation
    add column if not exists agent_id varchar(64);

update conversation
set agent_id = ''
where agent_id is null;

alter table conversation
    alter column agent_id set default '';

alter table conversation
    alter column agent_id set not null;

create index if not exists idx_conversation_agent_id on conversation (agent_id);
