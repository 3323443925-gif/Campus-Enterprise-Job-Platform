package com.example.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 面试通知表实体类
 */
@Data
public class InterviewNotice {
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 投递记录ID
     */
    private Long deliverId;
    
    /**
     * 对应岗位
     */
    private Long jobId;
    
    /**
     * 学生ID
     */
    private Long studentUserId;
    
    /**
     * 发送HR ID
     */
    private Long hrUserId;
    
    /**
     * 面试时间
     */
    private LocalDateTime interviewTime;
    
    /**
     * 面试地点/线上链接
     */
    private String interviewAddress;
    
    /**
     * 面试须知
     */
    private String interviewContent;
    
    /**
     * 学生是否已读
     */
    private Integer isRead;
    
    /**
     * 面试状态：0待确认 1已接受 2已拒绝
     */
    private Integer interviewStatus;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
