-- V1 初始表结构：登录注册 + 会话管理
create table if not exists user_account
(
    id            bigserial primary key,
    username      varchar(64)  not null,
    password_hash varchar(255) not null,
    created_at    timestamptz  not null default now(),
    updated_at    timestamptz  not null default now(),
    constraint uk_user_account_username unique (username)
);

create table if not exists conversation
(
    id         bigserial primary key,
    user_id    bigint       not null,
    title      varchar(255) not null,
    status     varchar(32)  not null default 'ACTIVE',
    created_at timestamptz  not null default now(),
    updated_at timestamptz  not null default now()
);

create index if not exists idx_conversation_user_id on conversation (user_id);
create index if not exists idx_conversation_created_at on conversation (created_at desc);
