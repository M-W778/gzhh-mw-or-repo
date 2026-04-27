USE or_sys;

INSERT INTO `t_user` (`id`, `username`, `password`, `phone`, `email`, `user_type`, `deletion_time`, `create_time`, `update_time`, `del_flag`)
VALUES
    (1, 'mw', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '13912345679', 'mw@example.com', 'PATIENT', 0, NOW(), NOW(), 0),
    (2, 'wm', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '13700137000', 'wm@example.com', 'PATIENT', 0, NOW(), NOW(), 0),
    (3, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '13912345678', 'admin@example.com', 'PATIENT', 0, NOW(), NOW(), 0);

INSERT INTO `t_patient` (`user_id`, `real_name`, `id_card`, `phone`, `gender`, `birth_date`, `is_default`) VALUES
                                                                                                               (1, '马一', '310101199208200048', '13912345679', 1, '1992-08-20', TRUE),
                                                                                                               (2, '马二', '310101198805150031', '13700137000', 0, '1988-05-15', FALSE),
                                                                                                               (3, '马三', '440106200410220024', '13912345678', 1, '2004-10-22', FALSE);

INSERT INTO `t_department` (`dept_name`, `description`, `icon_url`, `sort_order`) VALUES
                                                                                      ('内科', '负责心血管、呼吸、消化等系统疾病的诊断与治疗，提供慢性病管理及健康咨询服务', NULL, 1),
                                                                                      ('外科', '开展普外科、骨科、神经外科等手术治疗，涵盖创伤急救、微创手术及术后康复', NULL, 2),
                                                                                      ('儿科', '专注于新生儿、婴幼儿及儿童常见病的诊治与预防保健，提供儿童生长发育评估', NULL, 3),
                                                                                      ('妇产科', '提供妇科疾病诊疗、孕产期保健、产前检查及分娩服务，关注女性全生命周期健康', NULL, 4),
                                                                                      ('眼科', '开展视力检查、屈光矫正、白内障及青光眼等眼病的诊疗与手术，配备先进检查设备', NULL, 5);

-- 内科医生
INSERT INTO `t_doctor` (`department_id`, `name`, `title`, `specialty`, `introduction`, `avatar_url`, `registration_fee`) VALUES
                                                                                                                             (1, '陈景和', 'CHIEF', '冠心病介入治疗、心律失常、高血压', '从事心血管内科临床工作28年，擅长复杂冠脉介入手术及心脏起搏器植入，主持省级科研项目3项，发表SCI论文20余篇，获省级科技进步奖2项。', NULL, 50.00),
                                                                                                                             (1, '林雨桐', 'ASSOCIATE_CHIEF', '高血压病、心力衰竭、血脂异常', '心血管内科副主任医师，医学博士，专注于高血压慢病管理与心力衰竭综合治疗，参与多项国家级临床研究课题，擅长制定个体化降压方案。', NULL, 35.00);

-- 外科医生
INSERT INTO `t_doctor` (`department_id`, `name`, `title`, `specialty`, `introduction`, `avatar_url`, `registration_fee`) VALUES
                                                                                                                             (2, '高志远', 'CHIEF', '肝胆胰外科、腹腔镜微创手术、胃肠肿瘤', '普外科主任医师，教授，擅长肝胆胰恶性肿瘤根治及腹腔镜微创手术，年完成高难度手术300余台，带教硕士及博士研究生10余名，享受国务院特殊津贴。', NULL, 50.00),
                                                                                                                             (2, '方文博', 'ATTENDING', '甲状腺疾病、乳腺外科、疝气微创', '普外科主治医师，医学硕士，专注于甲状腺及乳腺疾病的规范化诊疗，熟练掌握腔镜甲状腺手术及乳腺微创旋切技术，注重患者术后生活质量。', NULL, 20.00);

-- 儿科医生
INSERT INTO `t_doctor` (`department_id`, `name`, `title`, `specialty`, `introduction`, `avatar_url`, `registration_fee`) VALUES
                                                                                                                             (3, '苏晓萌', 'CHIEF', '小儿哮喘、过敏性疾病、儿童免疫', '儿科主任医师，从事儿科临床与科研30年，在儿童哮喘及过敏性疾病的免疫治疗方面有深厚造诣，建立儿童过敏专病门诊，主持制定区域儿童哮喘诊疗指南。', NULL, 45.00),
                                                                                                                             (3, '杨思远', 'RESIDENT', '新生儿黄疸、儿童保健、常见病', '儿科住院医师，毕业于首都医科大学，擅长新生儿常见病诊治及儿童生长发育评估，对儿童营养与喂养指导有丰富经验，耐心细致深受家长信赖。', NULL, 15.00);

-- 妇产科医生
INSERT INTO `t_doctor` (`department_id`, `name`, `title`, `specialty`, `introduction`, `avatar_url`, `registration_fee`) VALUES
                                                                                                                             (4, '谢婉清', 'CHIEF', '高危妊娠、产前诊断、胎儿医学', '妇产科主任医师，产科学科带头人，擅长高危妊娠管理及胎儿医学，成功抢救多例危重孕产妇，建立产前诊断中心，获评省级三八红旗手及医德标兵。', NULL, 45.00),
                                                                                                                             (4, '何静姝', 'ASSOCIATE_CHIEF', '妇科微创、月经失调、多囊卵巢', '妇产科副主任医师，擅长宫腹腔镜微创手术及多囊卵巢综合征等月经失调疾病的内分泌治疗，在不孕不育的辅助生殖预处理方面积累了丰富的临床经验。', NULL, 30.00);

-- 眼科医生
INSERT INTO `t_doctor` (`department_id`, `name`, `title`, `specialty`, `introduction`, `avatar_url`, `registration_fee`) VALUES
                                                                                                                             (5, '沈锐锋', 'CHIEF', '屈光手术、角膜病、眼表疾病', '眼科主任医师，屈光手术中心主任，完成全飞秒及准分子激光手术逾万例，在角膜移植及干眼症等眼表疾病诊治方面经验丰富，担任省级眼科学会副主任委员。', NULL, 45.00),
                                                                                                                             (5, '唐悦然', 'ATTENDING', '斜弱视、儿童眼病、近视防控', '眼科主治医师，专注于儿童斜弱视矫治及青少年近视防控，擅长各类特殊眼镜验配及个性化视觉训练方案制定，在儿童低视力康复领域有独到见解。', NULL, 20.00);

SET @today = CURDATE();

-- 内科医生（号源 10-14 个）
INSERT INTO `t_schedule` (`doctor_id`, `schedule_date`, `time_slot`, `total_amount`, `remaining_amount`, `status`, `version`)
SELECT
    d.id AS doctor_id,
    DATE_ADD(@today, INTERVAL n.num DAY) AS schedule_date,
    ts.time_slot,
    FLOOR(10 + RAND() * 5) AS total_amount,
    FLOOR(10 + RAND() * 5) AS remaining_amount,
    'AVAILABLE' AS status,
    0 AS version
FROM `t_doctor` d
         CROSS JOIN (SELECT 1 AS num UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7) n
         CROSS JOIN (SELECT 'MORNING' AS time_slot UNION SELECT 'AFTERNOON') ts
WHERE d.department_id = 1 AND d.del_flag = 0;

-- 外科医生（号源 8-11 个）
INSERT INTO `t_schedule` (`doctor_id`, `schedule_date`, `time_slot`, `total_amount`, `remaining_amount`, `status`, `version`)
SELECT
    d.id AS doctor_id,
    DATE_ADD(@today, INTERVAL n.num DAY) AS schedule_date,
    ts.time_slot,
    FLOOR(8 + RAND() * 4) AS total_amount,
    FLOOR(8 + RAND() * 4) AS remaining_amount,
    'AVAILABLE' AS status,
    0 AS version
FROM `t_doctor` d
         CROSS JOIN (SELECT 1 AS num UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7) n
         CROSS JOIN (SELECT 'MORNING' AS time_slot UNION SELECT 'AFTERNOON') ts
WHERE d.department_id = 2 AND d.del_flag = 0;

-- 儿科医生（号源 12-18 个）
INSERT INTO `t_schedule` (`doctor_id`, `schedule_date`, `time_slot`, `total_amount`, `remaining_amount`, `status`, `version`)
SELECT
    d.id AS doctor_id,
    DATE_ADD(@today, INTERVAL n.num DAY) AS schedule_date,
    ts.time_slot,
    FLOOR(12 + RAND() * 7) AS total_amount,
    FLOOR(12 + RAND() * 7) AS remaining_amount,
    'AVAILABLE' AS status,
    0 AS version
FROM `t_doctor` d
         CROSS JOIN (SELECT 1 AS num UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7) n
         CROSS JOIN (SELECT 'MORNING' AS time_slot UNION SELECT 'AFTERNOON') ts
WHERE d.department_id = 3 AND d.del_flag = 0;

-- 妇产科医生（号源 10-14 个）
INSERT INTO `t_schedule` (`doctor_id`, `schedule_date`, `time_slot`, `total_amount`, `remaining_amount`, `status`, `version`)
SELECT
    d.id AS doctor_id,
    DATE_ADD(@today, INTERVAL n.num DAY) AS schedule_date,
    ts.time_slot,
    FLOOR(10 + RAND() * 5) AS total_amount,
    FLOOR(10 + RAND() * 5) AS remaining_amount,
    'AVAILABLE' AS status,
    0 AS version
FROM `t_doctor` d
         CROSS JOIN (SELECT 1 AS num UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7) n
         CROSS JOIN (SELECT 'MORNING' AS time_slot UNION SELECT 'AFTERNOON') ts
WHERE d.department_id = 4 AND d.del_flag = 0;

-- 眼科医生（号源 15-22 个）
INSERT INTO `t_schedule` (`doctor_id`, `schedule_date`, `time_slot`, `total_amount`, `remaining_amount`, `status`, `version`)
SELECT
    d.id AS doctor_id,
    DATE_ADD(@today, INTERVAL n.num DAY) AS schedule_date,
    ts.time_slot,
    FLOOR(15 + RAND() * 8) AS total_amount,
    FLOOR(15 + RAND() * 8) AS remaining_amount,
    'AVAILABLE' AS status,
    0 AS version
FROM `t_doctor` d
         CROSS JOIN (SELECT 1 AS num UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7) n
         CROSS JOIN (SELECT 'MORNING' AS time_slot UNION SELECT 'AFTERNOON') ts
WHERE d.department_id = 5 AND d.del_flag = 0;

-- notice seed data
INSERT INTO `t_notice` (`tag`, `tag_type`, `title`, `summary`, `publish_date`, `pinned`, `path`, `enabled`, `del_flag`, `create_time`, `update_time`) VALUES
          ('公告', 'primary', '五一假期门诊排班已发布，请提前预约就诊时段。', '节假日期间部分门诊时段有调整，请以预约页面实时号源为准。', '2026-04-27', 1, '/departments', 1, 0, NOW(), NOW()),
          ('通知', 'success', '线上预约支持“我的预约”内改签与取消操作。', '已支持在就诊前进行预约取消与状态查询。', '2026-04-25', 1, '/appointments', 1, 0, NOW(), NOW()),
        ('提醒', 'warning', '首次挂号请先完善就诊人实名信息并绑定手机号。', '为保障就诊准确核验，请在预约前补全实名信息。', '2026-04-24', 0, '/patients', 1, 0, NOW(), NOW()),
        ('就医', 'info', '请按预约时间提前15分钟到院签到，避免过号。', '建议预留交通时间，错峰就诊。', '2026-04-22', 0, '/departments', 1, 0, NOW(), NOW()),
          ('公告', 'primary', '发热门诊与急诊24小时开放，普通门诊按排班接诊。', '急危重症请优先选择急诊绿色通道。', '2026-04-20', 0, '/departments', 1, 0, NOW(), NOW()),
          ('通知', 'success', '检验检查报告可在个人中心在线查看。', '报告生成后可在系统中实时查询。', '2026-04-18', 0, '/profile', 1, 0, NOW(), NOW()),
          ('提醒', 'warning', '请携带身份证原件到院核验。', '部分科室还需携带既往病历资料。', '2026-04-15', 0, '/departments', 1, 0, NOW(), NOW()),
          ('就医', 'info', '儿科与妇产科高峰时段建议提前2-3天预约。', '热门科室号源更新频率较高，请及时关注。', '2026-04-12', 0, '/departments', 1, 0, NOW(), NOW());

SET @today = CURDATE();

-- 设置部分排班为已约满
UPDATE `t_schedule`
SET `status` = 'FULL', `remaining_amount` = 0
WHERE `id` IN (
    SELECT `id` FROM (
                         SELECT `id` FROM `t_schedule`
                         WHERE `schedule_date` = DATE_ADD(@today, INTERVAL 1 DAY)
                           AND `time_slot` = 'MORNING'
                         ORDER BY RAND()
                         LIMIT 4
                     ) tmp
);

-- 设置部分排班为停诊
UPDATE `t_schedule`
SET `status` = 'SUSPENDED'
WHERE `id` IN (
    SELECT `id` FROM (
                         SELECT `id` FROM `t_schedule`
                         WHERE `schedule_date` = DATE_ADD(@today, INTERVAL 2 DAY)
                           AND `time_slot` = 'AFTERNOON'
                         ORDER BY RAND()
                         LIMIT 5
                     ) tmp
);

SET @today = CURDATE();

-- 用户1（马一，patient_id=10）的预约
INSERT INTO `t_appointment` (
    `appointment_no`, `user_id`, `patient_id`, `doctor_id`, `schedule_id`,
    `appointment_date`, `time_slot`, `status`, `queue_number`, `remarks`, `notification_sent`
)
SELECT
    CONCAT('AP', DATE_FORMAT(NOW(), '%Y%m%d'), '0010'),
    1,
    10,
    s.doctor_id,
    s.id,
    s.schedule_date,
    s.time_slot,
    'PENDING',
    NULL,
    '马一测试预约',
    FALSE
FROM `t_schedule` s
WHERE s.schedule_date = DATE_ADD(@today, INTERVAL 1 DAY)
  AND s.status = 'AVAILABLE'
  AND s.remaining_amount > 0
LIMIT 1;

-- 用户2（马二，patient_id=11）的预约
INSERT INTO `t_appointment` (
    `appointment_no`, `user_id`, `patient_id`, `doctor_id`, `schedule_id`,
    `appointment_date`, `time_slot`, `status`, `queue_number`, `remarks`, `notification_sent`
)
SELECT
    CONCAT('AP', DATE_FORMAT(NOW(), '%Y%m%d'), '0011'),
    2,
    11,
    s.doctor_id,
    s.id,
    s.schedule_date,
    s.time_slot,
    'PENDING',
    NULL,
    '马二测试预约',
    FALSE
FROM `t_schedule` s
WHERE s.schedule_date = DATE_ADD(@today, INTERVAL 2 DAY)
  AND s.status = 'AVAILABLE'
  AND s.remaining_amount > 0
LIMIT 1;

-- 用户3（马三，patient_id=12）的预约
INSERT INTO `t_appointment` (
    `appointment_no`, `user_id`, `patient_id`, `doctor_id`, `schedule_id`,
    `appointment_date`, `time_slot`, `status`, `queue_number`, `remarks`, `notification_sent`
)
SELECT
    CONCAT('AP', DATE_FORMAT(NOW(), '%Y%m%d'), '0012'),
    3,
    12,
    s.doctor_id,
    s.id,
    s.schedule_date,
    s.time_slot,
    'PENDING',
    NULL,
    '马三测试预约',
    FALSE
FROM `t_schedule` s
WHERE s.schedule_date = DATE_ADD(@today, INTERVAL 3 DAY)
  AND s.status = 'AVAILABLE'
  AND s.remaining_amount > 0
LIMIT 1;


