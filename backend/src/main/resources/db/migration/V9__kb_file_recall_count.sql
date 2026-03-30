alter table kb_file
    add column if not exists recall_count integer not null default 0;

create index if not exists idx_kb_file_kb_user_status_created
    on kb_file (kb_id, user_id, parse_status, created_at desc)
    where del_flag = 0;

create index if not exists idx_kb_file_kb_user_recall
    on kb_file (kb_id, user_id, recall_count desc)
    where del_flag = 0;
