package com.example.service.impl;

import com.example.mapper.MajorMapper;
import com.example.pojo.Major;
import com.example.service.MajorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 专业服务实现类
 */
@Service
public class MajorServiceImpl implements MajorService {
    
    @Autowired
    private MajorMapper majorMapper;
    
    @Override
    public List<Major> findAll() {
        return majorMapper.findAll();
    }
    
    @Override
    public Major findById(Long id) {
        return majorMapper.findById(id);
    }
    
    @Override
    public Major findByMajorCode(String majorCode) {
        return majorMapper.findByMajorCode(majorCode);
    }
    
    @Override
    public List<Major> findByMajorName(String majorName) {
        return majorMapper.findByMajorName(majorName);
    }
    
    @Override
    public int insert(Major major) {
        return majorMapper.insert(major);
    }
    
    @Override
    public int update(Major major) {
        return majorMapper.update(major);
    }
    
    @Override
    public int deleteById(Long id) {
        return majorMapper.deleteById(id);
    }
}
