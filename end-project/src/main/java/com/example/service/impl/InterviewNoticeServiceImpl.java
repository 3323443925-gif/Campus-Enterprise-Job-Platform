package com.example.service.impl;

import com.example.mapper.InterviewNoticeMapper;
import com.example.pojo.InterviewNotice;
import com.example.pojo.RecruitJob;
import com.example.pojo.Enterprise;
import com.example.service.InterviewNoticeService;
import com.example.service.RecruitJobService;
import com.example.service.EnterpriseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * 面试通知服务实现类
 */
@Service
public class InterviewNoticeServiceImpl implements InterviewNoticeService {
    
    @Autowired
    private InterviewNoticeMapper interviewNoticeMapper;
    
    @Autowired
    private RecruitJobService recruitJobService;
    
    @Autowired
    private EnterpriseService enterpriseService;
    
    @Override
    public InterviewNotice findById(Long id) {
        return interviewNoticeMapper.findById(id);
    }
    
    @Override
    public List<InterviewNotice> findByDeliverId(Long deliverId) {
        return interviewNoticeMapper.findByDeliverId(deliverId);
    }
    
    @Override
    public List<InterviewNotice> findByJobId(Long jobId) {
        return interviewNoticeMapper.findByJobId(jobId);
    }
    
    @Override
    public List<InterviewNotice> findByStudentUserId(Long studentUserId) {
        return interviewNoticeMapper.findByStudentUserId(studentUserId);
    }
    
    @Override
    public List<InterviewNotice> findByHrUserId(Long hrUserId) {
        return interviewNoticeMapper.findByHrUserId(hrUserId);
    }
    
    @Override
    public List<InterviewNotice> findUnreadByStudentUserId(Long studentUserId) {
        return interviewNoticeMapper.findUnreadByStudentUserId(studentUserId);
    }
    
    @Override
    public int sendInterviewNotice(InterviewNotice interviewNotice) {
        // 默认未读
        if (interviewNotice.getIsRead() == null) {
            interviewNotice.setIsRead(0);
        }
        return interviewNoticeMapper.insert(interviewNotice);
    }
    
    @Override
    public int update(InterviewNotice interviewNotice) {
        return interviewNoticeMapper.update(interviewNotice);
    }
    
    @Override
    public int markAsRead(Long id) {
        return interviewNoticeMapper.markAsRead(id);
    }
    
    @Override
    public int markAllAsRead(Long studentUserId) {
        List<InterviewNotice> unreadList = interviewNoticeMapper.findUnreadByStudentUserId(studentUserId);
        int count = 0;
        for (InterviewNotice notice : unreadList) {
            count += interviewNoticeMapper.markAsRead(notice.getId());
        }
        return count;
    }
    
    @Override
    public int cancelInterview(Long id) {
        return interviewNoticeMapper.deleteById(id);
    }
    
    @Override
    public int rescheduleInterview(Long id, LocalDateTime newTime, String newAddress) {
        InterviewNotice notice = new InterviewNotice();
        notice.setId(id);
        notice.setInterviewTime(newTime);
        notice.setInterviewAddress(newAddress);
        return interviewNoticeMapper.update(notice);
    }
    
    @Override
    public int getUnreadCount(Long studentUserId) {
        List<InterviewNotice> unreadList = interviewNoticeMapper.findUnreadByStudentUserId(studentUserId);
        return unreadList != null ? unreadList.size() : 0;
    }

    @Override
    public List<InterviewNotice> findByStudentUserIdPage(Long studentUserId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return interviewNoticeMapper.findByStudentUserIdPage(studentUserId, offset, pageSize);
    }

    @Override
    public int countByStudentUserId(Long studentUserId) {
        return interviewNoticeMapper.countByStudentUserId(studentUserId);
    }

    @Override
    public List<InterviewNotice> findByHrUserIdPage(Long hrUserId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return interviewNoticeMapper.findByHrUserIdPage(hrUserId, offset, pageSize);
    }

    @Override
    public int countByHrUserId(Long hrUserId) {
        return interviewNoticeMapper.countByHrUserId(hrUserId);
    }
}
