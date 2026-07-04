package com.example.service;

import com.example.pojo.EnterpriseAudit;
import java.util.List;

/**
 * 企业资质审核服务接口
 */
public interface EnterpriseAuditService {
    
    /**
     * 根据ID查询审核记录
     */
    EnterpriseAudit findById(Long id);
    
    /**
     * 根据企业ID查询审核记录
     */
    List<EnterpriseAudit> findByEnterpriseId(Long enterpriseId);
    
    /**
     * 根据HR用户ID查询审核记录
     */
    List<EnterpriseAudit> findByHrUserId(Long hrUserId);
    
    /**
     * 根据审核状态查询
     */
    List<EnterpriseAudit> findByAuditStatus(Integer auditStatus);
    
    /**
     * 查询待审核记录
     */
    List<EnterpriseAudit> findPendingAudits();
    
    /**
     * 提交企业资质审核申请
     */
    int submitAudit(EnterpriseAudit enterpriseAudit);
    
    /**
     * 审核操作
     */
    int audit(Long id, Integer auditStatus, Long auditUserId, String auditRemark);
    
    /**
     * 通过审核
     */
    int approve(Long id, Long auditUserId, String auditRemark);
    
    /**
     * 驳回审核
     */
    int reject(Long id, Long auditUserId, String auditRemark);

    /**
     * 分页查询审核记录
     */
    List<EnterpriseAudit> findByPage(Integer auditStatus, int page, int pageSize);

    /**
     * 统计审核记录数量
     */
    int countByStatus(Integer auditStatus);
}
