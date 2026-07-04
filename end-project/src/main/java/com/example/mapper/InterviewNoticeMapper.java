package com.example.mapper;

import com.example.pojo.InterviewNotice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 面试通知表Mapper接口
 */
@Mapper
public interface InterviewNoticeMapper {
    
    /**
     * 查询所有面试通知
     */
    List<InterviewNotice> findAll();
    
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
     * 新增面试通知
     */
    int insert(InterviewNotice interviewNotice);
    
    /**
     * 更新面试通知
     */
    int update(InterviewNotice interviewNotice);
    
    /**
     * 标记为已读
     */
    int markAsRead(Long id);
    
    /**
     * 删除面试通知
     */
    int deleteById(Long id);

    /**
     * 分页查询学生的面试通知
     */
    List<InterviewNotice> findByStudentUserIdPage(@Param("studentUserId") Long studentUserId,
                                                  @Param("offset") int offset,
                                                  @Param("pageSize") int pageSize);

    /**
     * 统计学生的面试通知数量
     */
    int countByStudentUserId(@Param("studentUserId") Long studentUserId);

    /**
     * 分页查询 HR 的面试通知
     */
    List<InterviewNotice> findByHrUserIdPage(@Param("hrUserId") Long hrUserId,
                                              @Param("offset") int offset,
                                              @Param("pageSize") int pageSize);

    /**
     * 统计 HR 的面试通知数量
     */
    int countByHrUserId(@Param("hrUserId") Long hrUserId);
}
