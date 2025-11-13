CREATE TABLE bip_customer (
  id VARCHAR(36) PRIMARY KEY,
  customer_name VARCHAR(255),
  customer_code VARCHAR(255),
  customer_type VARCHAR(50),
  del_flag VARCHAR(2),
  created_by VARCHAR(100),
  create_time TIMESTAMP,
  update_by VARCHAR(100),
  update_time TIMESTAMP
);

CREATE TABLE bip_project (
  id VARCHAR(36) PRIMARY KEY,
  project_name VARCHAR(255),
  project_code VARCHAR(255),
  project_type VARCHAR(50),
  del_flag VARCHAR(2),
  created_by VARCHAR(100),
  create_time TIMESTAMP,
  update_by VARCHAR(100),
  update_time TIMESTAMP
);

CREATE TABLE bip_binding (
  id VARCHAR(36) PRIMARY KEY,
  customer_id VARCHAR(36),
  project_id VARCHAR(36),
  del_flag VARCHAR(2),
  created_by VARCHAR(100),
  create_time TIMESTAMP,
  update_by VARCHAR(100),
  update_time TIMESTAMP
);

CREATE TABLE bip_dashboard_statistics (
  id VARCHAR(36) PRIMARY KEY,
  "year" VARCHAR(50),
  "month" VARCHAR(50),
  area_id VARCHAR(36),
  area_name VARCHAR(255),
  retailer_name VARCHAR(255),
  retailer_id VARCHAR(36),
  task VARCHAR(255),
  sub_task VARCHAR(255),
  result VARCHAR(50),
  unit VARCHAR(20),
  remark VARCHAR(255),
  del_flag VARCHAR(2),
  created_by VARCHAR(100),
  create_time TIMESTAMP,
  update_by VARCHAR(100),
  update_time TIMESTAMP
);
