DROP TABLE IF EXISTS bip_binding ;
CREATE TABLE bip_binding (
   id varchar(30),
   customer_id varchar(30)  NULL default null ,
   project_id varchar(30)  NULL default null ,
  `del_flag` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT "软删除标志位",
 `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT "创建人",
 `create_time` datetime  NULL DEFAULT NULL COMMENT "创建时间",
 `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT "修改人",
 `update_time` datetime  NULL DEFAULT NULL COMMENT "修改时间",
  PRIMARY KEY (id) USING BTREE
);