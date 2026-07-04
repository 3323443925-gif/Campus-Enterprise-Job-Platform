package com.example.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业合作档案表实体类
 */
@Data
public class Enterprise {
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 企业名称
     */
    private String companyName;
    
    /**
     * 统一社会信用代码
     */
    private String unifiedSocialCode;
    
    /**
     * 企业地址
     */
    private String address;
    
    /**
     * 行业
     */
    private String industry;
    
    /**
     * 对接HR姓名
     */
    private String contactName;
    
    /**
     * 联系电话
     */
    private String contactPhone;
    
    /**
     * HR邮箱
     */
    private String contactEmail;
    
    /**
     * 合作等级 1普通2良好3核心
     */
    private Integer cooperationLevel;
    
    /**
     * 上次发布岗位时间
     */
    private LocalDateTime lastRecruitTime;
    
    /**
     * 累计发布岗位数
     */
    private Integer totalRecruitCount;
    
    /**
     * 累计录用学生数
     */
    private Integer totalEmployCount;
    
    /**
     * 流失预警 0正常1预警（超6个月未招聘）
     */
    private Integer isWarn;
    
    /**
     * 企业状态 0待审核1通过2驳回
     */
    private Integer enterpriseStatus;
    
    /**
     * 录入教师ID
     */
    private Long createUserId;
    
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
