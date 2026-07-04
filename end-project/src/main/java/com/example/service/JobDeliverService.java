package com.example.service;

import com.example.pojo.JobDeliver;
import java.util.List;

/**
 * 岗位投递服务接口
 */
public interface JobDeliverService {
    
    /**
     * 根据ID查询投递记录
     */
    JobDeliver findById(Long id);
    
    /**
     * 根据岗位ID查询投递记录
     */
    List<JobDeliver> findByJobId(Long jobId);
    
    /**
     * 根据学生用户ID查询投递记录
     */
    List<JobDeliver> findByStudentUserId(Long studentUserId);
    
    /**
     * 根据企业ID查询投递记录
     */
    List<JobDeliver> findByEnterpriseId(Long enterpriseId);
    
    /**
     * 根据投递状态查询
     */
    List<JobDeliver> findByDeliverStatus(Integer deliverStatus);
    
    /**
     * 根据追踪ID查询投递记录
     */
    List<JobDeliver> findByTrackId(String trackId);
    
    /**
     * 投递简历
     */
    int deliverJob(Long jobId, Long studentUserId, Long resumeId);
    
    /**
     * 更新投递状态
     */
    int updateDeliverStatus(Long id, Integer deliverStatus, String hrRemark);
    
    /**
     * 标记为待面试
     */
    int markAsInterview(Long id, String hrRemark);
    
    /**
     * 标记为不合适
     */
    int markAsRejected(Long id, String hrRemark);
    
    /**
     * 标记为已录用
     */
    int markAsHired(Long id, String hrRemark);
    
    /**
     * 标记为已报到
     */
    int markAsReported(Long id, String hrRemark);
    
    /**
     * 撤回投递
     */
    int withdrawDeliver(Long id);
    
    /**
     * 检查是否已投递
     */
    boolean hasDelivered(Long jobId, Long studentUserId);

    /**
     * 分页查询学生的投递记录
     */
    List<JobDeliver> findByStudentUserIdPage(Long studentUserId, int page, int pageSize);

    /**
     * 统计学生的投递记录数量
     */
    int countByStudentUserId(Long studentUserId);

    /**
     * 分页查询企业的投递记录
     */
    List<JobDeliver> findByEnterpriseIdPage(Long enterpriseId, Long jobId, Integer deliverStatus, int page, int pageSize);

    /**
     * 统计企业的投递记录数量
     */
    int countByEnterpriseId(Long enterpriseId, Long jobId, Integer deliverStatus);

    /**
     * 统计已就业学生数
     */
    int countEmployedStudents();

    /**
     * 分页查询教师发布岗位的投递记录
     */
    List<com.example.pojo.dto.DeliverDetailDTO> findByTeacherUserIdPage(Long teacherUserId, Long jobId, Integer deliverStatus, int page, int pageSize);

    /**
     * 统计教师发布岗位的投递记录数量
     */
    int countByTeacherUserId(Long teacherUserId, Long jobId, Integer deliverStatus);

    /**
     * 分页查询企业的投递记录（联表查询学生和简历信息）
     */
    List<com.example.pojo.dto.DeliverDetailDTO> findByEnterpriseIdPageWithDetail(Long enterpriseId, Long jobId, Integer deliverStatus, int page, int pageSize);

    /**
     * 统计薪资分布
     */
    List<java.util.Map<String, Object>> findSalaryStats();
}
