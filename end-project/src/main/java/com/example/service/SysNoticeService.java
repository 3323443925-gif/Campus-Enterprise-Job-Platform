package com.example.service;

import com.example.pojo.SysNotice;
import java.util.List;

/**
 * 站内消息服务接口
 */
public interface SysNoticeService {
    
    /**
     * 根据ID查询消息
     */
    SysNotice findById(Long id);
    
    /**
     * 根据接收人ID查询消息
     */
    List<SysNotice> findByReceiveUserId(Long receiveUserId);
    
    /**
     * 根据接收人ID查询未读消息
     */
    List<SysNotice> findUnreadByReceiveUserId(Long receiveUserId);
    
    /**
     * 根据消息类型查询
     */
    List<SysNotice> findByNoticeType(Integer noticeType);
    
    /**
     * 发送消息
     */
    int sendNotice(SysNotice sysNotice);
    
    /**
     * 批量发送消息
     */
    int batchSendNotices(List<SysNotice> notices);
    
    /**
     * 标记消息为已读
     */
    int markAsRead(Long id);
    
    /**
     * 标记所有消息为已读
     */
    int markAllAsRead(Long receiveUserId);
    
    /**
     * 删除消息
     */
    int deleteById(Long id);
    
    /**
     * 获取未读消息数量
     */
    int getUnreadCount(Long receiveUserId);

    /**
     * 分页查询接收人的消息
     */
    List<SysNotice> findByReceiveUserIdPage(Long receiveUserId, Integer isRead, int page, int pageSize);

    /**
     * 统计接收人的消息数量
     */
    int countByReceiveUserId(Long receiveUserId, Integer isRead);
}
