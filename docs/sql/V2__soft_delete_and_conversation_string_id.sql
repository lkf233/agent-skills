-- V2 增量DDL：伪删除、会话ID改为字符串、时间字段Java侧改为LocalDateTime（数据库仍为timestamptz）
alter table user_account
    add column if not exists del_flag smallint not null default 0;

alter table conversation
    add column if not exists del_flag smallint not null default 0;

alter table conversation
    drop constraint if exists fk_conversation_user_id;

alter table conversation
    alter column id type varchar(64) using id::varchar;

alter table conversation
    alter column id drop default;

drop sequence if exists conversation_id_seq;

create index if not exists idx_user_account_del_flag on user_account (del_flag);
create index if not exists idx_conversation_del_flag on conversation (del_flag);
