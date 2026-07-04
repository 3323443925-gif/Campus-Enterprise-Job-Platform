package com.example.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色表实体类
 */
@Data
public class SysRole {
    /**
     * 角色ID
     */
    private Long id;
    
    /**
     * 角色名称：管理员/就业办/教师/学生/企业HR
     */
    private String roleName;
    
    /**
     * 角色编码：admin/job_office/teacher/student/hr
     */
    private String roleCode;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 角色备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除 0未删1已删
     */
    private Integer isDeleted;
}
