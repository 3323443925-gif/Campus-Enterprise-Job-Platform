package com.example.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业资质审核表实体类
 */
@Data
public class EnterpriseAudit {
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 关联企业ID
     */
    private Long enterpriseId;
    
    /**
     * 申请HR用户ID
     */
    private Long hrUserId;
    
    /**
     * 营业执照图片路径
     */
    private String businessLicenseImg;
    
    /**
     * 审核状态 0待审核1通过2驳回
     */
    private Integer auditStatus;
    
    /**
     * 审核人（教师/管理员）
     */
    private Long auditUserId;
    
    /**
     * 审核意见
     */
    private String auditRemark;
    
    /**
     * 审核时间
     */
    private LocalDateTime auditTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
