package com.example.mapper;

import com.example.pojo.EnterpriseAudit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 企业资质审核表Mapper接口
 */
@Mapper
public interface EnterpriseAuditMapper {
    
    /**
     * 查询所有审核记录
     */
    List<EnterpriseAudit> findAll();
    
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
     * 新增审核记录
     */
    int insert(EnterpriseAudit enterpriseAudit);
    
    /**
     * 更新审核记录
     */
    int update(EnterpriseAudit enterpriseAudit);
    
    /**
     * 审核操作
     */
    int audit(Long id, Integer auditStatus, Long auditUserId, String auditRemark);

    /**
     * 分页查询审核记录（支持按状态筛选）
     */
    List<EnterpriseAudit> findByPage(@Param("auditStatus") Integer auditStatus,
                                     @Param("offset") int offset,
                                     @Param("pageSize") int pageSize);

    /**
     * 统计审核记录数量
     */
    int countByStatus(@Param("auditStatus") Integer auditStatus);
}
