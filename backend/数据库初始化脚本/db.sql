-- 创建数据库
CREATE DATABASE IF NOT EXISTS or_sys CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE or_sys;

-- 1. 用户表 t_user（phone 必填；username 在“未注销”维度唯一）
CREATE TABLE IF NOT EXISTS `t_user` (
                                        `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                        `username` varchar(256) NOT NULL COMMENT '用户名',
                                        `password` varchar(256) NOT NULL COMMENT '密码',
                                        `phone` varchar(20) NOT NULL COMMENT '手机号',
                                        `email` varchar(64) NOT NULL COMMENT '邮箱',
                                        `user_type` varchar(20) NOT NULL DEFAULT 'PATIENT' COMMENT '角色：PATIENT患者, ADMIN管理员',
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

-- 2. 就诊人表 t_patient
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

-- 3. 科室表 t_department
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

-- 4. 医生表 t_doctor
CREATE TABLE IF NOT EXISTS `t_doctor` (
                                          `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                          `department_id` bigint(20) unsigned NOT NULL COMMENT '所属科室ID',
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
                                          KEY `idx_department_id` (`department_id`) USING BTREE,
                                          KEY `idx_department_name` (`department_id`, `name`) USING BTREE,
                                          CONSTRAINT `fk_doctor_department` FOREIGN KEY (`department_id`) REFERENCES `t_department` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生表';

-- 5. 排班表 t_schedule（未来7天，每天上下午一条记录）
CREATE TABLE IF NOT EXISTS `t_schedule` (
                                            `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                            `doctor_id` bigint(20) unsigned NOT NULL COMMENT '医生ID',
                                            `schedule_date` date NOT NULL COMMENT '排班日期',
                                            `time_slot` varchar(10) NOT NULL COMMENT '时段：MORNING上午, AFTERNOON下午',
                                            `total_amount` int NOT NULL COMMENT '总号源数',
                                            `remaining_amount` int NOT NULL COMMENT '剩余号源数',
                                            `status` varchar(20) NOT NULL DEFAULT 'AVAILABLE' COMMENT '状态：AVAILABLE可预约, FULL已约满, SUSPENDED停诊',
                                            `version` bigint(20) NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
                                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                            `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                            PRIMARY KEY (`id`),
                                            UNIQUE KEY `uk_doctor_date_slot` (`doctor_id`, `schedule_date`, `time_slot`) USING BTREE,
                                            KEY `idx_doctor_date` (`doctor_id`, `schedule_date`) USING BTREE,
                                            CONSTRAINT `fk_schedule_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `t_doctor` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班表';

-- 6. 预约记录表 t_appointment
CREATE TABLE IF NOT EXISTS `t_appointment` (
                                               `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                               `appointment_no` varchar(20) NOT NULL COMMENT '预约号',
                                               `user_id` bigint(20) unsigned NOT NULL COMMENT '预约用户ID',
                                               `patient_id` bigint(20) unsigned NOT NULL COMMENT '就诊人ID',
                                               `doctor_id` bigint(20) unsigned NOT NULL COMMENT '医生ID',
                                               `schedule_id` bigint(20) unsigned NOT NULL COMMENT '排班ID',
                                               `appointment_date` date NOT NULL COMMENT '预约日期',
                                               `time_slot` varchar(10) NOT NULL COMMENT '时段',
                                               `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING待就诊, COMPLETED已完成, CANCELLED已取消, NO_SHOW爽约',
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
                                               CONSTRAINT `fk_appointment_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`),
                                               CONSTRAINT `fk_appointment_patient` FOREIGN KEY (`patient_id`) REFERENCES `t_patient` (`id`),
                                               CONSTRAINT `fk_appointment_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `t_doctor` (`id`),
                                               CONSTRAINT `fk_appointment_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `t_schedule` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约记录表';


USE or_sys;

-- =========================================================
-- 1) 预约表：冗余 department_id + 约束“同一就诊人同一科室同一天只能预约一次”
-- =========================================================

-- 1.1 新增字段 department_id（允许先为空，后面回填后再改 NOT NULL）
ALTER TABLE `t_appointment`
    ADD COLUMN `department_id` bigint(20) unsigned NULL COMMENT '科室ID(冗余，便于唯一约束与查询)'
        AFTER `doctor_id`;

-- 1.2 加外键（若历史数据未回填 department_id，此外键不影响；但建议回填后再把字段改 NOT NULL）
ALTER TABLE `t_appointment`
    ADD CONSTRAINT `fk_appointment_department`
        FOREIGN KEY (`department_id`) REFERENCES `t_department` (`id`);

-- 1.3 加索引（查询优化）
ALTER TABLE `t_appointment`
    ADD KEY `idx_dept_date` (`department_id`, `appointment_date`);

-- 1.4 唯一约束（注意：该唯一约束不区分 status，取消后的记录仍会占用唯一性；如需“取消后可再次预约”，需业务层放开/或改更复杂方案）
ALTER TABLE `t_appointment`
    ADD UNIQUE KEY `uk_patient_dept_date` (`patient_id`, `department_id`, `appointment_date`);

-- =========================================================
-- 2) 医生账号绑定：医生端登录/工作台需要 doctor <-> user 映射
-- =========================================================

-- 2.1 t_doctor 增加 user_id（一个医生绑定一个账号；可为空表示未开通医生账号）
ALTER TABLE `t_doctor`
    ADD COLUMN `user_id` bigint(20) unsigned NULL COMMENT '关联用户ID(医生登录账号)'
        AFTER `department_id`;

-- 2.2 唯一索引（1 个账号只能绑定 1 个医生）
ALTER TABLE `t_doctor`
    ADD UNIQUE KEY `uk_doctor_user_id` (`user_id`);

-- 2.3 外键
ALTER TABLE `t_doctor`
    ADD CONSTRAINT `fk_doctor_user`
        FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`);

-- =========================================================
-- 3) （可选）通知记录：如果只要“已发送”布尔值可不做；需要记录发送时间/渠道/内容才做
-- =========================================================
-- 仅用 ALTER TABLE 无法新建表；如你确认需要，我再给一段 CREATE TABLE t_notification + 外键/索引脚本。


-- 7. notice table
CREATE TABLE IF NOT EXISTS `t_notice` (
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `tag` varchar(20) NOT NULL COMMENT 'tag text',
    `tag_type` varchar(20) NOT NULL DEFAULT 'primary' COMMENT 'tag style',
    `title` varchar(120) NOT NULL COMMENT 'notice title',
    `summary` varchar(500) DEFAULT NULL COMMENT 'notice summary',
    `publish_date` date NOT NULL COMMENT 'publish date',
    `pinned` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'is pinned',
    `path` varchar(255) DEFAULT NULL COMMENT 'jump path',
    `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'enabled flag',
    `del_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'delete flag',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (`id`),
    KEY `idx_notice_publish` (`publish_date`) USING BTREE,
    KEY `idx_notice_enabled` (`enabled`) USING BTREE,
    KEY `idx_notice_pinned` (`pinned`) USING BTREE,
    KEY `idx_notice_del_flag` (`del_flag`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='notice table';
