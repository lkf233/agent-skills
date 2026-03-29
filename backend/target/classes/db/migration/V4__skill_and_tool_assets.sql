create table if not exists skill
(
    id                  varchar(64) primary key,
    user_id             bigint        not null,
    name                varchar(128)  not null,
    description         varchar(1024) not null default '',
    version             varchar(32)   not null default 'v1',
    schema_json         jsonb         not null default '{}'::jsonb,
    runtime_config_json jsonb         not null default '{}'::jsonb,
    status              varchar(32)   not null default 'DRAFT',
    created_at          timestamptz   not null default now(),
    updated_at          timestamptz   not null default now(),
    del_flag            smallint      not null default 0
);

create table if not exists tool_def
(
    id               varchar(64) primary key,
    user_id          bigint        not null,
    name             varchar(128)  not null,
    description      varchar(1024) not null default '',
    tool_type        varchar(32)   not null,
    config_json      jsonb         not null default '{}'::jsonb,
    auth_config_json jsonb         not null default '{}'::jsonb,
    status           varchar(32)   not null default 'DRAFT',
    created_at       timestamptz   not null default now(),
    updated_at       timestamptz   not null default now(),
    del_flag         smallint      not null default 0
);

create index if not exists idx_skill_user_del_updated on skill (user_id, del_flag, updated_at desc);
create index if not exists idx_skill_user_name_version on skill (user_id, name, version, del_flag);
create index if not exists idx_skill_status_del on skill (status, del_flag);

create index if not exists idx_tool_user_del_updated on tool_def (user_id, del_flag, updated_at desc);
create index if not exists idx_tool_user_type_del on tool_def (user_id, tool_type, del_flag);
create index if not exists idx_tool_status_del on tool_def (status, del_flag);
