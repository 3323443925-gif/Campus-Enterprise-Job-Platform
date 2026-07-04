package com.example.service;

import com.example.pojo.SysUserRole;
import java.util.List;

/**
 * 用户角色关联服务接口
 */
public interface SysUserRoleService {
    
    /**
     * 根据用户ID查询角色关联
     */
    List<SysUserRole> findByUserId(Long userId);
    
    /**
     * 根据角色ID查询用户关联
     */
    List<SysUserRole> findByRoleId(Long roleId);
    
    /**
     * 为用户分配角色
     */
    int assignRole(Long userId, Long roleId);
    
    /**
     * 批量为用户分配角色
     */
    int batchAssignRoles(Long userId, List<Long> roleIds);
    
    /**
     * 移除用户的所有角色
     */
    int removeRolesByUserId(Long userId);
    
    /**
     * 移除用户的指定角色
     */
    int removeRole(Long userId, Long roleId);
}
