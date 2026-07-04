package com.example.mapper;

import com.example.pojo.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 用户角色关联表Mapper接口
 */
@Mapper
public interface SysUserRoleMapper {
    
    /**
     * 查询所有用户角色关联
     */
    List<SysUserRole> findAll();
    
    /**
     * 根据用户ID查询角色关联
     */
    List<SysUserRole> findByUserId(Long userId);
    
    /**
     * 根据角色ID查询用户关联
     */
    List<SysUserRole> findByRoleId(Long roleId);
    
    /**
     * 新增用户角色关联
     */
    int insert(SysUserRole sysUserRole);
    
    /**
     * 批量新增用户角色关联
     */
    int batchInsert(List<SysUserRole> list);
    
    /**
     * 删除用户角色关联
     */
    int deleteByUserId(Long userId);
    
    /**
     * 根据用户ID和角色ID删除关联
     */
    int deleteByUserIdAndRoleId(Long userId, Long roleId);
}
