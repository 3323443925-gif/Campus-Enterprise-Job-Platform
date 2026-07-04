package com.example.service.impl;

import com.example.mapper.SysRoleMapper;
import com.example.pojo.SysRole;
import com.example.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 角色服务实现类
 */
@Service
public class SysRoleServiceImpl implements SysRoleService {
    
    @Autowired
    private SysRoleMapper sysRoleMapper;
    
    @Override
    public List<SysRole> findAll() {
        return sysRoleMapper.findAll();
    }
    
    @Override
    public SysRole findById(Long id) {
        return sysRoleMapper.findById(id);
    }
    
    @Override
    public SysRole findByRoleCode(String roleCode) {
        return sysRoleMapper.findByRoleCode(roleCode);
    }
    
    @Override
    public int insert(SysRole sysRole) {
        return sysRoleMapper.insert(sysRole);
    }
    
    @Override
    public int update(SysRole sysRole) {
        return sysRoleMapper.update(sysRole);
    }
    
    @Override
    public int deleteById(Long id) {
        return sysRoleMapper.deleteById(id);
    }
}
