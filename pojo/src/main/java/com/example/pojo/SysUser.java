package com.example.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户表实体类
 */
@Data
public class SysUser {
    /**
     * 用户主键
     */
    private Long id;

    /**
     * 登录账号：管理员账号/学生学号/企业手机号
     */
    private String username;

    /**
     * 加密密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 用户类型：student/teacher/admin/hr/job_office
     */
    private String userType;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 所属专业ID（学生/教师）
     */
    private Integer majorId;

    /**
     * 绑定企业ID（HR专用）
     */
    private Integer enterpriseId;
    
    /**
     * 账号状态 0禁用1正常
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除 0未删1已删
     */
    private Integer isDeleted;
}
