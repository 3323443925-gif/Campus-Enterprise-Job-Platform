package com.example.mapper;

import com.example.pojo.RecruitJob;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 招聘岗位表Mapper接口
 */
@Mapper
public interface RecruitJobMapper {
    
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
     * 新增岗位
     */
    int insert(RecruitJob recruitJob);
    
    /**
     * 更新岗位
     */
    int update(RecruitJob recruitJob);
    
    /**
     * 删除岗位（逻辑删除）
     */
    int deleteById(Long id);
    
    /**
     * 更新浏览次数
     */
    int updateViewCount(Long id);
    
    /**
     * 更新投递人数
     */
    int updateDeliverCount(Long id);

    /**
     * 分页查询已发布岗位（支持按薪资、地点、关键词筛选）
     */
    List<RecruitJob> findPublishedJobsByPage(@Param("salaryMin") Integer salaryMin,
                                              @Param("salaryMax") Integer salaryMax,
                                              @Param("workAddress") String workAddress,
                                              @Param("keyword") String keyword,
                                              @Param("offset") int offset,
                                              @Param("pageSize") int pageSize);

    /**
     * 统计已发布岗位数量
     */
    int countPublishedJobs(@Param("salaryMin") Integer salaryMin,
                           @Param("salaryMax") Integer salaryMax,
                           @Param("workAddress") String workAddress,
                           @Param("keyword") String keyword);

    /**
     * 分页查询企业岗位列表
     */
    List<RecruitJob> findByEnterpriseIdPage(@Param("enterpriseId") Long enterpriseId,
                                            @Param("offset") int offset,
                                            @Param("pageSize") int pageSize);

    /**
     * 统计企业岗位数量
     */
    int countByEnterpriseId(@Param("enterpriseId") Long enterpriseId);

    /**
     * 统计所有未删除的岗位总数
     */
    int countTotalJobs();

    /**
     * 分页查询教师发布的岗位列表
     */
    List<RecruitJob> findByTeacherUserIdPage(@Param("teacherUserId") Long teacherUserId,
                                             @Param("status") Integer status,
                                             @Param("offset") int offset,
                                             @Param("pageSize") int pageSize);

    /**
     * 统计教师发布的岗位数量
     */
    int countByTeacherUserId(@Param("teacherUserId") Long teacherUserId,
                             @Param("status") Integer status);

    /**
     * 统计各岗位类别投递数量（热门岗位排行）
     */
    List<java.util.Map<String, Object>> findJobCategoryStats();
}
