create extension if not exists vector;

alter table kb_chunk
    add column if not exists embedding vector(1024);

create index if not exists idx_kb_chunk_embedding_ivfflat
    on kb_chunk using ivfflat (embedding vector_cosine_ops) with (lists = 100);
