create extension if not exists "pgcrypto" schema public;

create table uploaded_videos
(
    id                   uuid        not null default gen_random_uuid() primary key,
    filename             text        not null,
    object_key           text        not null,
    conversion_status    text        not null,
    created_at           timestamptz not null,
    started_at           timestamptz,
    processed_at         timestamptz,
    worker_id            text,
    processed_object_key text,
    processed_object_url text,
    "order"              serial
);
