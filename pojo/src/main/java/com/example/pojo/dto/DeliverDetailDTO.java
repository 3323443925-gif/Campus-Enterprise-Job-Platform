package com.example.pojo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeliverDetailDTO {
    private Long id;
    private String trackId;
    private Long jobId;
    private String jobName;
    private Long enterpriseId;
    private Long studentUserId;
    private String studentName;
    private String studentNo;
    private Long majorId;
    private String majorName;
    private String expectCity;
    private Integer expectSalary;
    private String selfIntro;
    private String educationExp;
    private String practiceExp;
    private String certs;
    private String resumeFile;
    private Integer deliverStatus;
    private String hrRemark;
    private LocalDateTime deliverTime;
    private LocalDateTime createTime;
}
