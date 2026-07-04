package com.example.pojo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 招聘岗位表实体类
 */
@Data
public class RecruitJob {
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 唯一追踪ID，格式：JOB_年月日_随机串
     */
    private String trackId;
    
    /**
     * 所属企业
     */
    private Long enterpriseId;
    
    /**
     * 发布教师ID
     */
    private Long teacherUserId;
    
    /**
     * 招聘岗位名称
     */
    private String jobName;
    
    /**
     * 岗位类别：技术类/销售类/运营类等
     */
    private String jobCategory;
    
    /**
     * 最低薪资
     */
    private Integer salaryMin;
    
    /**
     * 最高薪资
     */
    private Integer salaryMax;
    
    /**
     * 工作地点
     */
    private String workAddress;
    
    /**
     * 学历要求
     */
    private String educationRequire;
    
    /**
     * 岗位要求、技能证书
     */
    private String jobRequire;
    
    /**
     * 岗位详情描述
     */
    private String jobDesc;
    
    /**
     * 招聘人数
     */
    private Integer recruitNum;
    
    /**
     * 招聘截止日期
     */
    private LocalDate deadline;
    
    /**
     * 状态 0草稿(AI解析未发布) 1已发布 2暂停招聘 3已关闭
     */
    private Integer status;
    
    /**
     * 浏览次数
     */
    private Integer viewCount;
    
    /**
     * 投递总人数
     */
    private Integer deliverCount;
    
    /**
     * HR是否可自主修改 0否1是
     */
    private Integer hrCanEdit;
    
    /**
     * HR修改是否需要教师审核 0免审1需审核
     */
    private Integer editNeedAudit;
    
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
