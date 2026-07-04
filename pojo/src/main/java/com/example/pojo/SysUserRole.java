package com.example.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户角色关联表实体类
 */
@Data
public class SysUserRole {
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 角色ID
     */
    private Long roleId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
