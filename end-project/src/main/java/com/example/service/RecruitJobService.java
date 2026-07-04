package com.example.service;

import com.example.pojo.RecruitJob;
import java.util.List;

/**
 * 招聘岗位服务接口
 */
public interface RecruitJobService {
    
    /**
     * 查询所有岗位
     */
    List<RecruitJob> findAll();
    
    /**
     * 根据ID查询岗位
     */
    RecruitJob findById(Long id);
    
    /**
     * 根据追踪ID查询岗位
     */
    RecruitJob findByTrackId(String trackId);
    
    /**
     * 根据企业ID查询岗位
     */
    List<RecruitJob> findByEnterpriseId(Long enterpriseId);
    
    /**
     * 根据教师ID查询岗位
     */
    List<RecruitJob> findByTeacherUserId(Long teacherUserId);
    
    /**
     * 根据岗位状态查询
     */
    List<RecruitJob> findByStatus(Integer status);
    
    /**
     * 根据岗位类别查询
     */
    List<RecruitJob> findByJobCategory(String jobCategory);
    
    /**
     * 查询已发布岗位
     */
    List<RecruitJob> findPublishedJobs();
    
    /**
     * 新增岗位（草稿状态）
     */
    int insert(RecruitJob recruitJob);

    /**
     * 发布岗位
     */
    int publishJob(RecruitJob recruitJob);
    
    /**
     * 更新岗位
     */
    int update(RecruitJob recruitJob);
    
    /**
     * 删除岗位（逻辑删除）
     */
    int deleteById(Long id);
    
    /**
     * 暂停招聘
     */
    int pauseRecruit(Long id);
    
    /**
     * 关闭招聘
     */
    int closeRecruit(Long id);
    
    /**
     * 更新浏览次数
     */
    int updateViewCount(Long id);
    
    /**
     * 更新投递人数
     */
    int updateDeliverCount(Long id);
    
    /**
     * 生成追踪ID
     */
    String generateTrackId();

    /**
     * 分页查询已发布岗位
     */
    List<RecruitJob> findPublishedJobsByPage(Integer salaryMin, Integer salaryMax, 
                                              String workAddress, String keyword, 
                                              int page, int pageSize);

    /**
     * 统计已发布岗位数量
     */
    int countPublishedJobs(Integer salaryMin, Integer salaryMax, 
                           String workAddress, String keyword);

    /**
     * 分页查询企业岗位列表
     */
    List<RecruitJob> findByEnterpriseIdPage(Long enterpriseId, int page, int pageSize);

    /**
     * 统计企业岗位数量
     */
    int countByEnterpriseId(Long enterpriseId);

    /**
     * 统计所有岗位总数
     */
    int countTotalJobs();

    /**
     * 分页查询教师发布的岗位列表
     */
    List<RecruitJob> findByTeacherUserIdPage(Long teacherUserId, Integer status, int page, int pageSize);

    /**
     * 统计教师发布的岗位数量
     */
    int countByTeacherUserId(Long teacherUserId, Integer status);

    /**
     * 统计各岗位类别投递数量（热门岗位排行）
     */
    List<java.util.Map<String, Object>> findJobCategoryStats();
}
