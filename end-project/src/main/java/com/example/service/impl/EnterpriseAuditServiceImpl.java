package com.example.service.impl;

import com.example.mapper.EnterpriseAuditMapper;
import com.example.mapper.EnterpriseMapper;
import com.example.pojo.EnterpriseAudit;
import com.example.pojo.Enterprise;
import com.example.service.EnterpriseAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 企业资质审核服务实现类
 */
@Service
public class EnterpriseAuditServiceImpl implements EnterpriseAuditService {
    
    @Autowired
    private EnterpriseAuditMapper enterpriseAuditMapper;
    
    @Autowired
    private EnterpriseMapper enterpriseMapper;
    
    @Override
    public EnterpriseAudit findById(Long id) {
        return enterpriseAuditMapper.findById(id);
    }
    
    @Override
    public List<EnterpriseAudit> findByEnterpriseId(Long enterpriseId) {
        return enterpriseAuditMapper.findByEnterpriseId(enterpriseId);
    }
    
    @Override
    public List<EnterpriseAudit> findByHrUserId(Long hrUserId) {
        return enterpriseAuditMapper.findByHrUserId(hrUserId);
    }
    
    @Override
    public List<EnterpriseAudit> findByAuditStatus(Integer auditStatus) {
        return enterpriseAuditMapper.findByAuditStatus(auditStatus);
    }
    
    @Override
    public List<EnterpriseAudit> findPendingAudits() {
        return enterpriseAuditMapper.findPendingAudits();
    }
    
    @Override
    public int submitAudit(EnterpriseAudit enterpriseAudit) {
        if (enterpriseAudit.getAuditStatus() == null) {
            enterpriseAudit.setAuditStatus(0);
        }
        return enterpriseAuditMapper.insert(enterpriseAudit);
    }
    
    @Override
    public int audit(Long id, Integer auditStatus, Long auditUserId, String auditRemark) {
        return enterpriseAuditMapper.audit(id, auditStatus, auditUserId, auditRemark);
    }
    
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public int approve(Long id, Long auditUserId, String auditRemark) {
        EnterpriseAudit audit = enterpriseAuditMapper.findById(id);
        if (audit == null) {
            return 0;
        }
        
        int result = enterpriseAuditMapper.audit(id, 1, auditUserId, auditRemark);
        
        if (result > 0 && audit.getEnterpriseId() != null) {
            Enterprise enterprise = enterpriseMapper.findById(audit.getEnterpriseId());
            if (enterprise != null) {
                enterprise.setEnterpriseStatus(1);
                enterpriseMapper.update(enterprise);
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public int reject(Long id, Long auditUserId, String auditRemark) {
        EnterpriseAudit audit = enterpriseAuditMapper.findById(id);
        if (audit == null) {
            return 0;
        }
        
        int result = enterpriseAuditMapper.audit(id, 2, auditUserId, auditRemark);
        
        if (result > 0 && audit.getEnterpriseId() != null) {
            Enterprise enterprise = enterpriseMapper.findById(audit.getEnterpriseId());
            if (enterprise != null) {
                enterprise.setEnterpriseStatus(2);
                enterpriseMapper.update(enterprise);
            }
        }
        
        return result;
    }

    @Override
    public List<EnterpriseAudit> findByPage(Integer auditStatus, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return enterpriseAuditMapper.findByPage(auditStatus, offset, pageSize);
    }

    @Override
    public int countByStatus(Integer auditStatus) {
        return enterpriseAuditMapper.countByStatus(auditStatus);
    }
}
