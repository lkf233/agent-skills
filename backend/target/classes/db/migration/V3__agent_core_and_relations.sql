create table if not exists agent
(
    id                varchar(64) primary key,
    user_id           bigint        not null,
    name              varchar(128)  not null,
    description       varchar(1024) not null default '',
    avatar_url        varchar(512)  not null default '',
    status            varchar(32)   not null default 'DRAFT',
    model_config_json jsonb         not null default '{}'::jsonb,
    created_at        timestamptz   not null default now(),
    updated_at        timestamptz   not null default now(),
    del_flag          smallint      not null default 0
);

create table if not exists agent_skill_rel
(
    id         varchar(64) primary key,
    agent_id    varchar(64) not null,
    skill_id    varchar(64) not null,
    priority    int         not null default 100,
    enabled     smallint    not null default 1,
    created_at  timestamptz not null default now(),
    updated_at  timestamptz not null default now(),
    del_flag    smallint    not null default 0
);

create table if not exists agent_tool_rel
(
    id         varchar(64) primary key,
    agent_id    varchar(64) not null,
    tool_id     varchar(64) not null,
    priority    int         not null default 100,
    enabled     smallint    not null default 1,
    created_at  timestamptz not null default now(),
    updated_at  timestamptz not null default now(),
    del_flag    smallint    not null default 0
);

create table if not exists agent_kb_rel
(
    id         varchar(64) primary key,
    agent_id    varchar(64) not null,
    kb_id       varchar(64) not null,
    priority    int         not null default 100,
    enabled     smallint    not null default 1,
    created_at  timestamptz not null default now(),
    updated_at  timestamptz not null default now(),
    del_flag    smallint    not null default 0
);

alter table conversation
    add column if not exists agent_id varchar(64) not null default '';

create index if not exists idx_agent_user_del_updated on agent (user_id, del_flag, updated_at desc);
create index if not exists idx_agent_status_del on agent (status, del_flag);
create unique index if not exists uk_agent_user_name_del on agent (user_id, name, del_flag);

create index if not exists idx_agent_skill_rel_agent_priority on agent_skill_rel (agent_id, del_flag, priority);
create index if not exists idx_agent_skill_rel_skill_del on agent_skill_rel (skill_id, del_flag);
create unique index if not exists uk_agent_skill_rel_active on agent_skill_rel (agent_id, skill_id, del_flag);

create index if not exists idx_agent_tool_rel_agent_priority on agent_tool_rel (agent_id, del_flag, priority);
create index if not exists idx_agent_tool_rel_tool_del on agent_tool_rel (tool_id, del_flag);
create unique index if not exists uk_agent_tool_rel_active on agent_tool_rel (agent_id, tool_id, del_flag);

create index if not exists idx_agent_kb_rel_agent_priority on agent_kb_rel (agent_id, del_flag, priority);
create index if not exists idx_agent_kb_rel_kb_del on agent_kb_rel (kb_id, del_flag);
create unique index if not exists uk_agent_kb_rel_active on agent_kb_rel (agent_id, kb_id, del_flag);

create index if not exists idx_conversation_agent_id on conversation (agent_id);
