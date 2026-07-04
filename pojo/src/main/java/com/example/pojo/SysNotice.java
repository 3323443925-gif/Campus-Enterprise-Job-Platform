package com.example.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站内消息表实体类
 */
@Data
public class SysNotice {
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 接收人ID
     */
    private Long receiveUserId;
    
    /**
     * 消息标题
     */
    private String title;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息类型 1岗位通知 2面试通知 3审核通知 4预警通知
     */
    private Integer noticeType;
    
    /**
     * 关联业务ID：岗位/投递/企业
     */
    private Long relatedId;
    
    /**
     * 是否已读 0未读1已读
     */
    private Integer isRead;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
