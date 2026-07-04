package com.example.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 专业表实体类
 */
@Data
public class Major {
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 专业名称
     */
    private String majorName;
    
    /**
     * 专业编码
     */
    private String majorCode;
    
    /**
     * 备注
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
