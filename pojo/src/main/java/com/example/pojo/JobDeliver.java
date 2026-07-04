package com.example.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 岗位投递记录表实体类
 */
@Data
public class JobDeliver {
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 岗位追踪ID
     */
    private String trackId;
    
    /**
     * 岗位ID
     */
    private Long jobId;
    
    /**
     * 企业ID
     */
    private Long enterpriseId;
    
    /**
     * 投递学生ID
     */
    private Long studentUserId;
    
    /**
     * 简历ID
     */
    private Long resumeId;
    
    /**
     * 投递时间
     */
    private LocalDateTime deliverTime;
    
    /**
     * 投递状态 0待处理 1待面试 2面试完成 3不合适 4已录用 5已报到
     */
    private Integer deliverStatus;
    
    /**
     * HR备注
     */
    private String hrRemark;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
