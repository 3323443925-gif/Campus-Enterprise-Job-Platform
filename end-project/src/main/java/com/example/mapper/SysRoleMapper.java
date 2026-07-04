package com.example.mapper;

import com.example.pojo.SysRole;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 角色表Mapper接口
 */
@Mapper
public interface SysRoleMapper {
    
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
