CREATE DATABASE IF NOT EXISTS school_employment_platform
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE school_employment_platform;

CREATE TABLE sys_role (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
                          role_name VARCHAR(50) NOT NULL COMMENT '角色名称：管理员/就业办/教师/学生/企业HR',
                          role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码：admin/job_office/teacher/student/hr',
                          sort INT DEFAULT 0 COMMENT '排序',
                          remark VARCHAR(255) COMMENT '角色备注',
                          create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删1已删'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 初始化角色
INSERT INTO sys_role(role_name,role_code) VALUES
                                              ('系统管理员','admin'),
                                              ('就业办教师','job_office'),
                                              ('专业班主任','teacher'),
                                              ('在校学生','student'),
                                              ('企业HR','hr');

CREATE TABLE sys_user (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户主键',
                          username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号：管理员账号/学生学号/企业手机号',
                          password VARCHAR(100) NOT NULL COMMENT '加密密码',
                          real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
                          user_type VARCHAR(20) NOT NULL COMMENT '用户类型：student/teacher/admin/hr/job_office',
                          phone VARCHAR(20) COMMENT '手机号',
                          email VARCHAR(100) COMMENT '邮箱',
                          avatar VARCHAR(255) COMMENT '头像地址',
                          major_id BIGINT DEFAULT NULL COMMENT '所属专业ID（学生/教师）',
                          enterprise_id BIGINT DEFAULT NULL COMMENT '绑定企业ID（HR专用）',
                          status TINYINT DEFAULT 1 COMMENT '账号状态 0禁用1正常',
                          create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                          update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          is_deleted TINYINT DEFAULT 0,
                          KEY idx_user_type(user_type),
                          KEY idx_major(major_id),
                          KEY idx_enterprise(enterprise_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

CREATE TABLE sys_user_role (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id BIGINT NOT NULL COMMENT '用户ID',
                               role_id BIGINT NOT NULL COMMENT '角色ID',
                               create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                               UNIQUE KEY uk_user_role(user_id,role_id),
                               KEY idx_role_id(role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

CREATE TABLE sys_notice (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            receive_user_id BIGINT NOT NULL COMMENT '接收人ID',
                            title VARCHAR(100) NOT NULL COMMENT '消息标题',
                            content TEXT NOT NULL COMMENT '消息内容',
                            notice_type TINYINT NOT NULL COMMENT '1岗位通知 2面试通知 3审核通知 4预警通知',
                            related_id BIGINT COMMENT '关联业务ID：岗位/投递/企业',
                            is_read TINYINT DEFAULT 0 COMMENT '0未读1已读',
                            create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息表';

CREATE TABLE major (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       major_name VARCHAR(100) NOT NULL COMMENT '专业名称',
                       major_code VARCHAR(50) UNIQUE NOT NULL COMMENT '专业编码',
                       remark VARCHAR(255),
                       create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                       update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       is_deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业表';

CREATE TABLE enterprise (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            company_name VARCHAR(200) NOT NULL COMMENT '企业名称',
                            unified_social_code VARCHAR(50) COMMENT '统一社会信用代码',
                            address VARCHAR(255) COMMENT '企业地址',
                            industry VARCHAR(100) COMMENT '行业',
                            contact_name VARCHAR(50) COMMENT '对接HR姓名',
                            contact_phone VARCHAR(20) COMMENT '联系电话',
                            contact_email VARCHAR(100) COMMENT 'HR邮箱',
                            cooperation_level TINYINT DEFAULT 1 COMMENT '合作等级 1普通2良好3核心',
                            last_recruit_time DATETIME COMMENT '上次发布岗位时间',
                            total_recruit_count INT DEFAULT 0 COMMENT '累计发布岗位数',
                            total_employ_count INT DEFAULT 0 COMMENT '累计录用学生数',
                            is_warn TINYINT DEFAULT 0 COMMENT '流失预警 0正常1预警（超6个月未招聘）',
                            enterprise_status TINYINT NOT NULL DEFAULT 0 COMMENT '0待审核1通过2驳回',
                            create_user_id BIGINT NOT NULL COMMENT '录入教师ID',
                            create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                            update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            is_deleted TINYINT DEFAULT 0,
                            KEY idx_company_name(company_name),
                            KEY idx_create_user(create_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业合作档案表';

CREATE TABLE enterprise_audit (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  enterprise_id BIGINT NOT NULL COMMENT '关联企业ID',
                                  hr_user_id BIGINT NOT NULL COMMENT '申请HR用户ID',
                                  business_license_img VARCHAR(255) NOT NULL COMMENT '营业执照图片路径',
                                  audit_status TINYINT NOT NULL DEFAULT 0 COMMENT '0待审核1通过2驳回',
                                  audit_user_id BIGINT COMMENT '审核人（教师/管理员）',
                                  audit_remark VARCHAR(255) COMMENT '审核意见',
                                  audit_time DATETIME COMMENT '审核时间',
                                  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业资质审核表';

CREATE TABLE recruit_job (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             track_id VARCHAR(64) NOT NULL UNIQUE COMMENT '唯一追踪ID，格式：JOB_年月日_随机串',
                             enterprise_id BIGINT COMMENT '所属企业',
                             teacher_user_id BIGINT COMMENT '发布教师ID',
                             job_name VARCHAR(100) NOT NULL COMMENT '招聘岗位名称',
                             job_category VARCHAR(50) COMMENT '岗位类别：技术类/销售类/运营类等',
                             salary_min INT COMMENT '最低薪资',
                             salary_max INT COMMENT '最高薪资',
                             work_address VARCHAR(255) COMMENT '工作地点',
                             education_require VARCHAR(50) COMMENT '学历要求',
                             job_require TEXT COMMENT '岗位要求、技能证书',
                             job_desc TEXT COMMENT '岗位详情描述',
                             recruit_num INT DEFAULT 1 COMMENT '招聘人数',
                             deadline DATE COMMENT '招聘截止日期',
                             status TINYINT NOT NULL DEFAULT 0 COMMENT '0草稿(AI解析未发布) 1已发布 2暂停招聘 3已关闭',
                             view_count INT DEFAULT 0 COMMENT '浏览次数',
                             deliver_count INT DEFAULT 0 COMMENT '投递总人数',
                             hr_can_edit TINYINT DEFAULT 1 COMMENT 'HR是否可自主修改 0否1是',
                             edit_need_audit TINYINT DEFAULT 1 COMMENT 'HR修改是否需要教师审核 0免审1需审核',
                             create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                             update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             is_deleted TINYINT DEFAULT 0,
                             KEY idx_enterprise(enterprise_id),
                             KEY idx_teacher(teacher_user_id),
                             KEY idx_status(status),
                             KEY idx_category(job_category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='招聘岗位表';

CREATE TABLE student_resume (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                student_user_id BIGINT NOT NULL UNIQUE COMMENT '学生用户ID',
                                major_id BIGINT NOT NULL COMMENT '所学专业',
                                student_no VARCHAR(50) NOT NULL COMMENT '学号',
                                expect_city VARCHAR(100) COMMENT '期望工作城市',
                                expect_salary INT COMMENT '期望薪资',
                                job_status TINYINT DEFAULT 0 COMMENT '求职状态：0未投递 1投递中 2已拿offer 3已签约',
                                self_intro TEXT COMMENT '自我评价',
                                education_exp TEXT COMMENT '教育经历',
                                practice_exp TEXT COMMENT '实习经历',
                                certs TEXT COMMENT '技能证书，逗号分隔',
                                resume_file VARCHAR(255) COMMENT '上传PDF简历地址',
                                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                KEY idx_major(major_id),
                                KEY idx_job_status(job_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生简历表';

CREATE TABLE job_deliver (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             track_id VARCHAR(64) NOT NULL COMMENT '岗位追踪ID',
                             job_id BIGINT NOT NULL COMMENT '岗位ID',
                             enterprise_id BIGINT COMMENT '企业ID',
                             student_user_id BIGINT NOT NULL COMMENT '投递学生ID',
                             resume_id BIGINT NOT NULL COMMENT '简历ID',
                             deliver_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '投递时间',
                             deliver_status TINYINT NOT NULL DEFAULT 0 COMMENT '0待处理 1待面试 2面试完成 3不合适 4已录用 5已报到',
                             hr_remark VARCHAR(500) COMMENT 'HR备注',
                             create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                             update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             KEY idx_job(job_id),
                             KEY idx_student(student_user_id),
                             KEY idx_status(deliver_status),
                             UNIQUE KEY uk_job_student(job_id,student_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位投递记录表';

CREATE TABLE interview_notice (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  deliver_id BIGINT NOT NULL COMMENT '投递记录ID',
                                  job_id BIGINT NOT NULL COMMENT '对应岗位',
                                  student_user_id BIGINT NOT NULL COMMENT '学生',
                                  hr_user_id BIGINT NOT NULL COMMENT '发送HR',
                                  interview_time DATETIME NOT NULL COMMENT '面试时间',
                                  interview_address VARCHAR(255) NOT NULL COMMENT '面试地点/线上链接',
                                  interview_content TEXT COMMENT '面试须知',
                                  is_read TINYINT DEFAULT 0 COMMENT '学生是否已读',
                                  interview_status TINYINT DEFAULT 0 COMMENT '面试状态：0待确认 1已接受 2已拒绝',
                                  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试通知表';