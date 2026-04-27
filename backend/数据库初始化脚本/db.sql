-- Current schema baseline (aligned with backend/数据库初始化脚本/db.sql)
CREATE DATABASE IF NOT EXISTS or_sys CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE or_sys;

--用户表
CREATE TABLE IF NOT EXISTS `t_user` (
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `username` varchar(256) NOT NULL COMMENT '用户名',
    `password` varchar(256) NOT NULL COMMENT '密码',
    `phone` varchar(20) NOT NULL COMMENT '手机号',
    `email` varchar(64) NOT NULL COMMENT '邮箱',
    `user_type` varchar(20) NOT NULL DEFAULT 'PATIENT' COMMENT '角色：PATIENT/DOCTOR/ADMIN/BAN',
    `deletion_time` bigint(20) NOT NULL DEFAULT '0' COMMENT '注销时间戳(0表示未注销)',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标识(0未删,1已删)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`) USING BTREE,
    UNIQUE KEY `uk_username_deletion_time` (`username`, `deletion_time`) USING BTREE,
    UNIQUE KEY `uk_email_deletion_time` (`email`, `deletion_time`) USING BTREE,
    KEY `idx_username` (`username`) USING BTREE,
    KEY `idx_phone` (`phone`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

--就诊人表
CREATE TABLE IF NOT EXISTS `t_patient` (
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` bigint(20) unsigned NOT NULL COMMENT '关联用户ID',
    `real_name` varchar(50) NOT NULL COMMENT '姓名',
    `id_card` varchar(18) NOT NULL COMMENT '身份证号',
    `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
    `gender` tinyint(1) NOT NULL COMMENT '性别：0女,1男',
    `birth_date` date DEFAULT NULL COMMENT '出生日期',
    `is_default` boolean NOT NULL DEFAULT FALSE COMMENT '是否默认就诊人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_id_card` (`id_card`) USING BTREE,
    KEY `idx_user_id` (`user_id`) USING BTREE,
    CONSTRAINT `fk_patient_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='就诊人表';

--科室表
CREATE TABLE IF NOT EXISTS `t_department` (
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `dept_name` varchar(50) NOT NULL COMMENT '科室名称',
    `description` varchar(255) DEFAULT NULL COMMENT '科室描述',
    `icon_url` varchar(255) DEFAULT NULL COMMENT '图标路径',
    `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标识(0未删,1已删)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dept_name` (`dept_name`) USING BTREE,
    KEY `idx_sort_order` (`sort_order`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室表';

--医生表
CREATE TABLE IF NOT EXISTS `t_doctor` (
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `department_id` bigint(20) unsigned NOT NULL COMMENT '所属科室ID',
    `user_id` bigint(20) unsigned DEFAULT NULL COMMENT '关联用户ID(医生登录账号)',
    `name` varchar(50) NOT NULL COMMENT '医生姓名',
    `title` varchar(20) NOT NULL COMMENT '职称：CHIEF主任医师, ASSOCIATE_CHIEF副主任医师, ATTENDING主治医师, RESIDENT住院医师',
    `specialty` varchar(200) DEFAULT NULL COMMENT '专长',
    `introduction` text COMMENT '简介',
    `avatar_url` varchar(255) DEFAULT NULL COMMENT '头像路径',
    `registration_fee` decimal(19,2) NOT NULL DEFAULT 0.00 COMMENT '挂号费(元)',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标识(0未删,1已删)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_doctor_user_id` (`user_id`) USING BTREE,
    KEY `idx_department_id` (`department_id`) USING BTREE,
    KEY `idx_department_name` (`department_id`, `name`) USING BTREE,
    CONSTRAINT `fk_doctor_department` FOREIGN KEY (`department_id`) REFERENCES `t_department` (`id`),
    CONSTRAINT `fk_doctor_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生表';

--排班表(号源表)
CREATE TABLE IF NOT EXISTS `t_schedule` (
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `doctor_id` bigint(20) unsigned NOT NULL COMMENT '医生ID',
    `schedule_date` date NOT NULL COMMENT '排班日期',
    `time_slot` varchar(10) NOT NULL COMMENT '时段：MORNING/AFTERNOON',
    `total_amount` int NOT NULL COMMENT '总号源数',
    `remaining_amount` int NOT NULL COMMENT '剩余号源数',
    `status` varchar(20) NOT NULL DEFAULT 'AVAILABLE' COMMENT '状态：AVAILABLE可预约/FULL已约满/SUSPENDED停诊',
    `version` bigint(20) NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_doctor_date_slot` (`doctor_id`, `schedule_date`, `time_slot`) USING BTREE,
    KEY `idx_doctor_date` (`doctor_id`, `schedule_date`) USING BTREE,
    CONSTRAINT `fk_schedule_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `t_doctor` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班表';

--预约记录表
CREATE TABLE IF NOT EXISTS `t_appointment` (
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `appointment_no` varchar(20) NOT NULL COMMENT '预约号',
    `user_id` bigint(20) unsigned NOT NULL COMMENT '预约用户ID',
    `patient_id` bigint(20) unsigned NOT NULL COMMENT '就诊人ID',
    `doctor_id` bigint(20) unsigned NOT NULL COMMENT '医生ID',
    `department_id` bigint(20) unsigned DEFAULT NULL COMMENT '科室ID(冗余)',
    `schedule_id` bigint(20) unsigned NOT NULL COMMENT '排班ID',
    `appointment_date` date NOT NULL COMMENT '预约日期',
    `time_slot` varchar(10) NOT NULL COMMENT '时段',
    `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING待就诊/COMPLETED已完成/CANCELLED已取消/EXCEED已超时',
    `queue_number` int DEFAULT NULL COMMENT '排队号',
    `remarks` varchar(500) DEFAULT NULL COMMENT '备注',
    `refuse_time` datetime DEFAULT NULL COMMENT '取消时间',
    `refuse_reason` varchar(200) DEFAULT NULL COMMENT '取消原因',
    `notification_sent` boolean NOT NULL DEFAULT FALSE COMMENT '是否已发送通知',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_appointment_no` (`appointment_no`) USING BTREE,
    KEY `idx_user_id` (`user_id`) USING BTREE,
    KEY `idx_patient_id` (`patient_id`) USING BTREE,
    KEY `idx_doctorid_appointmentdate` (`doctor_id`, `appointment_date`) USING BTREE,
    KEY `idx_dept_date` (`department_id`, `appointment_date`) USING BTREE,
    CONSTRAINT `fk_appointment_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`),
    CONSTRAINT `fk_appointment_patient` FOREIGN KEY (`patient_id`) REFERENCES `t_patient` (`id`),
    CONSTRAINT `fk_appointment_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `t_doctor` (`id`),
    CONSTRAINT `fk_appointment_department` FOREIGN KEY (`department_id`) REFERENCES `t_department` (`id`),
    CONSTRAINT `fk_appointment_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `t_schedule` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约记录表';

--通知表
CREATE TABLE IF NOT EXISTS `t_notice` (
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `tag` varchar(20) NOT NULL COMMENT '内容',
    `tag_type` varchar(20) NOT NULL DEFAULT 'primary' COMMENT '类型',
    `title` varchar(120) NOT NULL COMMENT '通知标题',
    `summary` varchar(500) DEFAULT NULL COMMENT '通知总结',
    `publish_date` date NOT NULL COMMENT '发送日期',
    `pinned` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否固定',
    `path` varchar(255) DEFAULT NULL COMMENT '路径',
    `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '禁用标识',
    `del_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标识(0未删,1已删)',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '跟新时间',
    PRIMARY KEY (`id`),
    KEY `idx_notice_publish` (`publish_date`) USING BTREE,
    KEY `idx_notice_enabled` (`enabled`) USING BTREE,
    KEY `idx_notice_pinned` (`pinned`) USING BTREE,
    KEY `idx_notice_del_flag` (`del_flag`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- =========================================================
-- Legacy compatibility migration (idempotent)
-- Drop uk_patient_dept_date to allow re-booking after cancellation.
-- =========================================================
SET @idx_exists = (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 't_appointment'
      AND index_name = 'uk_patient_dept_date'
);

SET @ddl = IF(
    @idx_exists > 0,
    'ALTER TABLE `t_appointment` DROP INDEX `uk_patient_dept_date`',
    'SELECT ''uk_patient_dept_date already dropped'' AS message'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
