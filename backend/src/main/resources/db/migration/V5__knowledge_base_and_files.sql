create table if not exists knowledge_base
(
    id                 varchar(64) primary key,
    user_id            bigint        not null,
    name               varchar(128)  not null,
    description        varchar(1024) not null default '',
    embedding_provider varchar(64)   not null,
    embedding_model    varchar(128)  not null,
    status             varchar(32)   not null default 'ACTIVE',
    created_at         timestamptz   not null default now(),
    updated_at         timestamptz   not null default now(),
    del_flag           smallint      not null default 0
);

create table if not exists kb_file
(
    id            varchar(64) primary key,
    kb_id         varchar(64)   not null,
    user_id       bigint        not null,
    file_name     varchar(255)  not null,
    storage_path  varchar(1024) not null,
    mime_type     varchar(128)  not null,
    size_bytes    bigint        not null,
    parse_status  varchar(32)   not null default 'UPLOADED',
    error_message varchar(1024) not null default '',
    created_at    timestamptz   not null default now(),
    updated_at    timestamptz   not null default now(),
    del_flag      smallint      not null default 0
);

create index if not exists idx_kb_user_del_updated on knowledge_base (user_id, del_flag, updated_at desc);
create index if not exists idx_kb_status_del on knowledge_base (status, del_flag);

create index if not exists idx_kb_file_kb_del_created on kb_file (kb_id, del_flag, created_at desc);
create index if not exists idx_kb_file_user_parse_del on kb_file (user_id, parse_status, del_flag);
