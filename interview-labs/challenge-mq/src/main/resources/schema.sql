create table if not exists users (
  id varchar(64) primary key,
  status varchar(32)
);

create table if not exists outbox (
  id bigserial primary key,
  aggregate_type varchar(64),
  aggregate_id varchar(64),
  type varchar(128),
  payload text,
  created_at timestamptz,
  status varchar(32)
);

create table if not exists inbox (
  message_id bigint primary key,
  processed_at timestamptz
);

create table if not exists notifications (
  user_id varchar(64) primary key,
  count int
);

create table if not exists kafka_inbox (
  message_key varchar(128) primary key,
  processed_at timestamptz
);

create table if not exists kafka_notifications (
  user_id varchar(64) primary key,
  count int
);

create table if not exists rocket_inbox (
  message_key varchar(128) primary key,
  processed_at timestamptz
);

create table if not exists rocket_notifications (
  user_id varchar(64) primary key,
  count int
);

create table if not exists lab04_inbox (
  message_id varchar(128) primary key,
  processed_at timestamptz
);

create table if not exists lab04_counts (
  sku varchar(128) primary key,
  count int
);

create table if not exists lab05_log (
  k varchar(128),
  seq int,
  ts timestamptz,
  primary key(k, seq)
);

create table if not exists lab06_counts (
  user_id varchar(64) primary key,
  count int
);

create table if not exists lab07_ws (
  user_id varchar(64),
  instance_id varchar(64),
  count int,
  primary key(user_id, instance_id)
);
