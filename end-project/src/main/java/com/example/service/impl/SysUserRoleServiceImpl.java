package com.example.service.impl;

import com.example.mapper.SysUserRoleMapper;
import com.example.pojo.SysUserRole;
import com.example.service.SysUserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户角色关联服务实现类
 */
@Service
public class SysUserRoleServiceImpl implements SysUserRoleService {
    
    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;
    
    @Override
    public List<SysUserRole> findByUserId(Long userId) {
        return sysUserRoleMapper.findByUserId(userId);
    }
    
    @Override
    public List<SysUserRole> findByRoleId(Long roleId) {
        return sysUserRoleMapper.findByRoleId(roleId);
    }
    
    @Override
    public int assignRole(Long userId, Long roleId) {
        SysUserRole sysUserRole = new SysUserRole();
        sysUserRole.setUserId(userId);
        sysUserRole.setRoleId(roleId);
        return sysUserRoleMapper.insert(sysUserRole);
    }
    
    @Override
    public int batchAssignRoles(Long userId, List<Long> roleIds) {
        List<SysUserRole> list = new ArrayList<>();
        for (Long roleId : roleIds) {
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setUserId(userId);
            sysUserRole.setRoleId(roleId);
            list.add(sysUserRole);
        }
        return sysUserRoleMapper.batchInsert(list);
    }
    
    @Override
    public int removeRolesByUserId(Long userId) {
        return sysUserRoleMapper.deleteByUserId(userId);
    }
    
    @Override
    public int removeRole(Long userId, Long roleId) {
        return sysUserRoleMapper.deleteByUserIdAndRoleId(userId, roleId);
    }
}
