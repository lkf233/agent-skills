create table if not exists kb_chunk
(
    id             varchar(64) primary key,
    kb_id          varchar(64)   not null,
    kb_file_id     varchar(64)   not null,
    user_id        bigint        not null,
    chunk_no       int           not null,
    content        text          not null,
    content_tsv    tsvector      not null,
    embedding_json jsonb         not null default '[]'::jsonb,
    created_at     timestamptz   not null default now(),
    updated_at     timestamptz   not null default now(),
    del_flag       smallint      not null default 0
);

create table if not exists kb_ingest_task
(
    id            varchar(64) primary key,
    kb_file_id    varchar(64)   not null,
    user_id       bigint        not null,
    task_status   varchar(32)   not null default 'PENDING',
    retry_count   int           not null default 0,
    error_message varchar(1024) not null default '',
    created_at    timestamptz   not null default now(),
    updated_at    timestamptz   not null default now(),
    del_flag      smallint      not null default 0
);

create unique index if not exists uk_kb_chunk_file_chunk on kb_chunk (kb_file_id, chunk_no, del_flag);
create index if not exists idx_kb_chunk_kb_del on kb_chunk (kb_id, del_flag);
create index if not exists idx_kb_chunk_file_del on kb_chunk (kb_file_id, del_flag);
create index if not exists idx_kb_chunk_content_tsv on kb_chunk using gin (content_tsv);

create unique index if not exists uk_kb_ingest_task_file_del on kb_ingest_task (kb_file_id, del_flag);
create index if not exists idx_kb_ingest_task_status_updated on kb_ingest_task (task_status, updated_at);
