package com.example.service;

import com.example.pojo.InterviewNotice;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 面试通知服务接口
 */
public interface InterviewNoticeService {
    
    /**
     * 根据ID查询面试通知
     */
    InterviewNotice findById(Long id);
    
    /**
     * 根据投递记录ID查询面试通知
     */
    List<InterviewNotice> findByDeliverId(Long deliverId);
    
    /**
     * 根据岗位ID查询面试通知
     */
    List<InterviewNotice> findByJobId(Long jobId);
    
    /**
     * 根据学生用户ID查询面试通知
     */
    List<InterviewNotice> findByStudentUserId(Long studentUserId);
    
    /**
     * 根据HR用户ID查询面试通知
     */
    List<InterviewNotice> findByHrUserId(Long hrUserId);
    
    /**
     * 查询学生的未读面试通知
     */
    List<InterviewNotice> findUnreadByStudentUserId(Long studentUserId);
    
    /**
     * 发送面试通知
     */
    int sendInterviewNotice(InterviewNotice interviewNotice);
    
    /**
     * 更新面试通知
     */
    int update(InterviewNotice interviewNotice);
    
    /**
     * 标记为已读
     */
    int markAsRead(Long id);
    
    /**
     * 标记所有为已读
     */
    int markAllAsRead(Long studentUserId);
    
    /**
     * 取消面试
     */
    int cancelInterview(Long id);
    
    /**
     * 修改面试时间
     */
    int rescheduleInterview(Long id, LocalDateTime newTime, String newAddress);
    
    /**
     * 获取未读面试通知数量
     */
    int getUnreadCount(Long studentUserId);

    /**
     * 分页查询学生的面试通知
     */
    List<InterviewNotice> findByStudentUserIdPage(Long studentUserId, int page, int pageSize);

    /**
     * 统计学生的面试通知数量
     */
    int countByStudentUserId(Long studentUserId);

    /**
     * 分页查询 HR 的面试通知
     */
    List<InterviewNotice> findByHrUserIdPage(Long hrUserId, int page, int pageSize);

    /**
     * 统计 HR 的面试通知数量
     */
    int countByHrUserId(Long hrUserId);
}
