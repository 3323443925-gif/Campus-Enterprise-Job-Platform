package com.example.service;

import com.example.pojo.SysRole;
import java.util.List;

/**
 * 角色服务接口
 */
public interface SysRoleService {
    
    /**
     * 查询所有角色
     */
    List<SysRole> findAll();
    
    /**
     * 根据ID查询角色
     */
    SysRole findById(Long id);
    
    /**
     * 根据角色编码查询角色
     */
    SysRole findByRoleCode(String roleCode);
    
    /**
     * 新增角色
     */
    int insert(SysRole sysRole);
    
    /**
     * 更新角色
     */
    int update(SysRole sysRole);
    
    /**
     * 删除角色（逻辑删除）
     */
    int deleteById(Long id);
}
