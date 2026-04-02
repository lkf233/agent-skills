create table if not exists conversation_message
(
    id            varchar(64) primary key,
    conversation_id varchar(64)  not null,
    user_id       bigint       not null,
    role          varchar(32)  not null,
    content       text         not null default '',
    metadata_json jsonb        not null default '{}'::jsonb,
    token_input   int          not null default 0,
    token_output  int          not null default 0,
    seq_no        int          not null,
    created_at    timestamptz  not null default now(),
    updated_at    timestamptz  not null default now(),
    del_flag      smallint     not null default 0
);

create table if not exists conversation_summary
(
    id               varchar(64) primary key,
    conversation_id  varchar(64)  not null,
    range_start_seq  int          not null,
    range_end_seq    int          not null,
    summary_text     text         not null default '',
    summary_struct_json jsonb     not null default '{}'::jsonb,
    version          int          not null default 1,
    created_at       timestamptz  not null default now(),
    updated_at       timestamptz  not null default now(),
    del_flag         smallint     not null default 0
);

create table if not exists tool_manifest_snapshot
(
    id            varchar(64) primary key,
    tool_def_id   varchar(64)  not null,
    manifest_json jsonb        not null default '{}'::jsonb,
    manifest_hash varchar(128) not null default '',
    fetched_at    timestamptz  not null default now(),
    expire_at     timestamptz  not null,
    status        varchar(32)  not null default 'READY',
    error_message varchar(1024) not null default '',
    created_at    timestamptz  not null default now(),
    updated_at    timestamptz  not null default now(),
    del_flag      smallint     not null default 0
);

create table if not exists tool_call_log
(
    id             varchar(64) primary key,
    conversation_id varchar(64)  not null,
    message_id     varchar(64)  not null default '',
    tool_def_id    varchar(64)  not null,
    tool_name      varchar(128) not null default '',
    request_json   jsonb        not null default '{}'::jsonb,
    response_json  jsonb        not null default '{}'::jsonb,
    status         varchar(32)  not null default 'SUCCESS',
    latency_ms     bigint       not null default 0,
    created_at     timestamptz  not null default now()
);

create index if not exists idx_conversation_message_conv_seq
    on conversation_message (conversation_id, del_flag, seq_no desc);

create index if not exists idx_conversation_message_user_created
    on conversation_message (user_id, del_flag, created_at desc);

create unique index if not exists uk_conversation_message_conv_seq
    on conversation_message (conversation_id, seq_no, del_flag);

create index if not exists idx_conversation_summary_conv_range
    on conversation_summary (conversation_id, del_flag, range_end_seq desc);

create index if not exists idx_tool_manifest_snapshot_tool_expire
    on tool_manifest_snapshot (tool_def_id, del_flag, expire_at desc);

create index if not exists idx_tool_call_log_conv_created
    on tool_call_log (conversation_id, created_at desc);
