/*
 Navicat Premium Dump SQL

 Source Server         : czh
 Source Server Type    : MySQL
 Source Server Version : 80025 (8.0.25)
 Source Host           : localhost:3306
 Source Schema         : upload-file

 Target Server Type    : MySQL
 Target Server Version : 80025 (8.0.25)
 File Encoding         : 65001

 Date: 09/03/2025 00:03:50
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for storage_config
-- ----------------------------
DROP TABLE IF EXISTS `storage_config`;
CREATE TABLE `storage_config`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '配置id',
  `type` enum('local','minio','oss','obs') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '存储类型(\'local\',\'minio\',\'oss\',\'obs\')',
  `endpoint` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '访问地址',
  `access_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '用户名',
  `secret_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '密码',
  `bucket` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '桶',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `status+name`(`access_key` ASC) USING BTREE,
  INDEX `name`(`access_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of storage_config
-- ----------------------------
INSERT INTO `storage_config` VALUES (1, 'local', 'http://localhost:10086', '', '', 'E:\\develop\\Java\\xxxxxx\\upload-file-backend\\data');
INSERT INTO `storage_config` VALUES (2, 'minio', 'http://localhost:9000', 'minio', 'minio123', 'upload-file');
INSERT INTO `storage_config` VALUES (3, 'oss', 'https://oss-cn-guangzhou.aliyuncs.com', 'ossAccessKeyID', 'ossAccessKeySecret', 'yourBucket');

-- ----------------------------
-- Table structure for upload_file
-- ----------------------------
DROP TABLE IF EXISTS `upload_file`;
CREATE TABLE `upload_file`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `upload_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分片上传的uploadId',
  `file_identifier` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件唯一标识（md5）',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件名',
  `object_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件的key',
  `total_size` bigint NOT NULL COMMENT '文件大小（byte）',
  `chunk_size` bigint NOT NULL COMMENT '每个分片大小（byte）',
  `chunk_num` int NOT NULL COMMENT '分片数量',
  `is_finish` int NOT NULL COMMENT '是否已完成上传(完成合并),1是0否',
  `content_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件类型',
  `access_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '访问地址',
  `download_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '下载地址',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `storage_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上传类型',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uq_file_identifier`(`file_identifier` ASC) USING BTREE,
  UNIQUE INDEX `uq_upload_id`(`upload_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '上传文件记录表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
