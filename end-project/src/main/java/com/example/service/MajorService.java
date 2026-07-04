package com.example.service;

import com.example.pojo.Major;
import java.util.List;

/**
 * 专业服务接口
 */
public interface MajorService {
    
    /**
     * 查询所有专业
     */
    List<Major> findAll();
    
    /**
     * 根据ID查询专业
     */
    Major findById(Long id);
    
    /**
     * 根据专业编码查询专业
     */
    Major findByMajorCode(String majorCode);
    
    /**
     * 根据专业名称模糊查询
     */
    List<Major> findByMajorName(String majorName);
    
    /**
     * 新增专业
     */
    int insert(Major major);
    
    /**
     * 更新专业
     */
    int update(Major major);
    
    /**
     * 删除专业（逻辑删除）
     */
    int deleteById(Long id);
}
