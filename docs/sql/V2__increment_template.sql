-- V2 增量DDL模板（示例）
-- 说明：后续结构变更必须以增量脚本追加，不允许直接修改 V1 脚本。

-- 示例1：新增字段
-- alter table conversation add column archived_at timestamptz;

-- 示例2：新增索引
-- create index if not exists idx_conversation_status on conversation (status);

-- 示例3：新增表
-- create table if not exists user_memory_profile
-- (
--     id         bigserial primary key,
--     user_id    bigint      not null,
--     memory_key varchar(64) not null,
--     memory_val text        not null,
--     created_at timestamptz not null default now(),
--     updated_at timestamptz not null default now()
-- );
