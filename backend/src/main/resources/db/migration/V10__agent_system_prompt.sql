alter table agent
    add column if not exists system_prompt text not null default '';
