
DROP TABLE IF EXISTS `data_statistics`

CREATE TABLE `bip_dashboard_statistics` (
 `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci `  NOT NULL    COMMENT "abc",
 `year` varchar(50) CHARACTER SET utm8mb4 COLLATE utm8mb4_general_ci NOT NULL COMMENT "abc",
 `month` varchar(50) CHARACTER SET utm8mb4 COLLATE utm8mb4_general_ci NOT NULL COMMENT "abc",
 `area_id` varchar(50) CHARACTER SET utm8mb4 COLLATE utm8mb4_general_ci NOT NULL COMMENT "abc",
 `area_name` varchar(50) CHARACTER SET utm8mb4 COLLATE utm8mb4_general_ci NOT NULL COMMENT "abc",
 `retailer_name` varchar(50) CHARACTER SET utm8mb4 COLLATE utm8mb4_general_ci NOT NULL COMMENT "abc",
 `retailer_id` varchar(50) CHARACTER SET utm8mb4 COLLATE utm8mb4_general_ci NOT NULL COMMENT "abc",
 `task` varchar(50) CHARACTER SET utm8mb4 COLLATE utm8mb4_general_ci NOT NULL COMMENT "abc",
 `sub_task` varchar(50) CHARACTER SET utm8mb4 COLLATE utm8mb4_general_ci NOT NULL COMMENT "abc",
 `result` varchar(50) CHARACTER SET utm8mb4 COLLATE utm8mb4_general_ci NOT NULL COMMENT "abc",
 `unit` varchar(50) CHARACTER SET utm8mb4 COLLATE utm8mb4_general_ci NOT NULL COMMENT "abc",
 `remark` varchar(50) CHARACTER SET utm8mb4 COLLATE utm8mb4_general_ci NOT NULL COMMENT "abc",
 `del_flag` varchar(50) CHARACTER SET utm8mb4 COLLATE utm8mb4_general_ci NOT NULL COMMENT "abc",
 `created_by` varchar(50) CHARACTER SET utm8mb4 COLLATE utm8mb4_general_ci NOT NULL COMMENT "abc",
 `create_time` varchar(50) CHARACTER SET utm8mb4 COLLATE utm8mb4_general_ci NOT NULL COMMENT "abc",
 `update_by` varchar(50) CHARACTER SET utm8mb4 COLLATE utm8mb4_general_ci NOT NULL COMMENT "abc",
 `update_time` varchar(50) CHARACTER SET utm8mb4 COLLATE utm8mb4_general_ci NOT NULL COMMENT "abc",
 PRIMARY KEY(`id`)  USING BTREE
)  ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;