package com.example.service.impl;

import com.example.mapper.SysNoticeMapper;
import com.example.pojo.SysNotice;
import com.example.service.SysNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 站内消息服务实现类
 */
@Service
public class SysNoticeServiceImpl implements SysNoticeService {
    
    @Autowired
    private SysNoticeMapper sysNoticeMapper;
    
    @Override
    public SysNotice findById(Long id) {
        return sysNoticeMapper.findById(id);
    }
    
    @Override
    public List<SysNotice> findByReceiveUserId(Long receiveUserId) {
        return sysNoticeMapper.findByReceiveUserId(receiveUserId);
    }
    
    @Override
    public List<SysNotice> findUnreadByReceiveUserId(Long receiveUserId) {
        return sysNoticeMapper.findUnreadByReceiveUserId(receiveUserId);
    }
    
    @Override
    public List<SysNotice> findByNoticeType(Integer noticeType) {
        return sysNoticeMapper.findByNoticeType(noticeType);
    }
    
    @Override
    public int sendNotice(SysNotice sysNotice) {
        // 默认未读
        if (sysNotice.getIsRead() == null) {
            sysNotice.setIsRead(0);
        }
        return sysNoticeMapper.insert(sysNotice);
    }
    
    @Override
    public int batchSendNotices(List<SysNotice> notices) {
        int count = 0;
        for (SysNotice notice : notices) {
            count += sendNotice(notice);
        }
        return count;
    }
    
    @Override
    public int markAsRead(Long id) {
        return sysNoticeMapper.markAsRead(id);
    }
    
    @Override
    public int markAllAsRead(Long receiveUserId) {
        List<SysNotice> unreadList = sysNoticeMapper.findUnreadByReceiveUserId(receiveUserId);
        int count = 0;
        for (SysNotice notice : unreadList) {
            count += sysNoticeMapper.markAsRead(notice.getId());
        }
        return count;
    }
    
    @Override
    public int deleteById(Long id) {
        return sysNoticeMapper.deleteById(id);
    }
    
    @Override
    public int getUnreadCount(Long receiveUserId) {
        List<SysNotice> unreadList = sysNoticeMapper.findUnreadByReceiveUserId(receiveUserId);
        return unreadList != null ? unreadList.size() : 0;
    }

    @Override
    public List<SysNotice> findByReceiveUserIdPage(Long receiveUserId, Integer isRead, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return sysNoticeMapper.findByReceiveUserIdPage(receiveUserId, isRead, offset, pageSize);
    }

    @Override
    public int countByReceiveUserId(Long receiveUserId, Integer isRead) {
        return sysNoticeMapper.countByReceiveUserId(receiveUserId, isRead);
    }
}
