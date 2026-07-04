package com.example.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生简历表实体类
 */
@Data
public class StudentResume {
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 学生用户ID
     */
    private Long studentUserId;
    
    /**
     * 所学专业
     */
    private Long majorId;
    
    /**
     * 学号
     */
    private String studentNo;
    
    /**
     * 期望工作城市
     */
    private String expectCity;
    
    /**
     * 期望薪资
     */
    private Integer expectSalary;
    
    /**
     * 求职状态：0未投递 1投递中 2已拿offer 3已签约
     */
    private Integer jobStatus;
    
    /**
     * 自我评价
     */
    private String selfIntro;
    
    /**
     * 教育经历
     */
    private String educationExp;
    
    /**
     * 实习经历
     */
    private String practiceExp;
    
    /**
     * 技能证书，逗号分隔
     */
    private String certs;
    
    /**
     * 上传PDF简历地址
     */
    private String resumeFile;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
