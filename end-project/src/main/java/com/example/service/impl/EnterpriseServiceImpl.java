package com.example.service.impl;

import com.example.mapper.EnterpriseMapper;
import com.example.pojo.Enterprise;
import com.example.service.EnterpriseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 企业服务实现类
 */
@Service
public class EnterpriseServiceImpl implements EnterpriseService {
    
    @Autowired
    private EnterpriseMapper enterpriseMapper;
    
    @Override
    public List<Enterprise> findAll() {
        return enterpriseMapper.findAll();
    }
    
    @Override
    public Enterprise findById(Long id) {
        return enterpriseMapper.findById(id);
    }
    
    @Override
    public List<Enterprise> findByCompanyName(String companyName) {
        return enterpriseMapper.findByCompanyName(companyName);
    }
    
    @Override
    public List<Enterprise> findByIndustry(String industry) {
        return enterpriseMapper.findByIndustry(industry);
    }
    
    @Override
    public List<Enterprise> findByCreateUserId(Long createUserId) {
        return enterpriseMapper.findByCreateUserId(createUserId);
    }
    
    @Override
    public List<Enterprise> findWarnEnterprises() {
        return enterpriseMapper.findWarnEnterprises();
    }
    
    @Override
    public int insert(Enterprise enterprise) {
        // 默认合作等级为普通
        if (enterprise.getCooperationLevel() == null) {
            enterprise.setCooperationLevel(1);
        }
        // 默认非预警状态
        if (enterprise.getIsWarn() == null) {
            enterprise.setIsWarn(0);
        }
        return enterpriseMapper.insert(enterprise);
    }
    
    @Override
    public int update(Enterprise enterprise) {
        return enterpriseMapper.update(enterprise);
    }
    
    @Override
    public int deleteById(Long id) {
        return enterpriseMapper.deleteById(id);
    }
    
    @Override
    public int updateRecruitCount(Long id) {
        return enterpriseMapper.updateRecruitCount(id);
    }
    
    @Override
    public int updateEmployCount(Long id) {
        return enterpriseMapper.updateEmployCount(id);
    }
    
    @Override
    public int setWarnStatus(Long id, Integer isWarn) {
        Enterprise enterprise = new Enterprise();
        enterprise.setId(id);
        enterprise.setIsWarn(isWarn);
        return enterpriseMapper.update(enterprise);
    }

    @Override
    public List<Enterprise> findByCondition(String keyword, String industry) {
        return enterpriseMapper.findByCondition(keyword, industry);
    }

    @Override
    public int countTotal() {
        return enterpriseMapper.countTotal();
    }

    @Override
    public List<java.util.Map<String, Object>> findIndustryStats() {
        return enterpriseMapper.findIndustryStats();
    }
}
