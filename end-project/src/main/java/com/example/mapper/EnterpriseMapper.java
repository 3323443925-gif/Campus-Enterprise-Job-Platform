package com.example.mapper;

import com.example.pojo.Enterprise;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 企业合作档案表Mapper接口
 */
@Mapper
public interface EnterpriseMapper {
    
    /**
     * 查询所有企业
     */
    List<Enterprise> findAll();
    
    /**
     * 根据ID查询企业
     */
    Enterprise findById(Long id);
    
    /**
     * 根据企业名称模糊查询
     */
    List<Enterprise> findByCompanyName(String companyName);
    
    /**
     * 根据行业查询企业
     */
    List<Enterprise> findByIndustry(String industry);
    
    /**
     * 根据录入教师ID查询企业
     */
    List<Enterprise> findByCreateUserId(Long createUserId);
    
    /**
     * 查询预警企业
     */
    List<Enterprise> findWarnEnterprises();
    
    /**
     * 新增企业
     */
    int insert(Enterprise enterprise);
    
    /**
     * 更新企业
     */
    int update(Enterprise enterprise);
    
    /**
     * 删除企业（逻辑删除）
     */
    int deleteById(Long id);
    
    /**
     * 更新累计招聘数
     */
    int updateRecruitCount(Long id);
    
    /**
     * 更新累计录用数
     */
    int updateEmployCount(Long id);

    /**
     * 组合条件查询企业列表（支持关键词、行业筛选）
     */
    List<Enterprise> findByCondition(@Param("keyword") String keyword,
                                     @Param("industry") String industry);

    /**
     * 统计企业总数
     */
    int countTotal();

    /**
     * 统计各行业企业数量
     */
    List<java.util.Map<String, Object>> findIndustryStats();
}
