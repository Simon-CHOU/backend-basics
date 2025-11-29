create table if not exists project (
  id varchar(36) primary key,
  project_code varchar(64),
  bu_type varchar(64),
  project_type varchar(64),
  status varchar(32)
);

create table if not exists project_process (
  id varchar(36) primary key,
  project_id varchar(36) not null,
  process_code varchar(64) not null,
  status varchar(32) not null,
  constraint fk_pp_project foreign key(project_id) references project(id)
);

create table if not exists project_task (
  id varchar(36) primary key,
  process_id varchar(36) not null,
  process_node_code varchar(128) not null,
  assign_role_name varchar(64),
  assign_role_id varchar(64),
  status varchar(32) not null,
  result varchar(32),
  meta text,
  constraint fk_pt_process foreign key(process_id) references project_process(id)
);
