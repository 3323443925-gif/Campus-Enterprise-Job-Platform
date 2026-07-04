package com.example.mapper;

import com.example.pojo.JobDeliver;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 岗位投递记录表Mapper接口
 */
@Mapper
public interface JobDeliverMapper {
    
    /**
     * 查询所有投递记录
     */
    List<JobDeliver> findAll();
    
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
     * 查询学生的特定岗位投递记录
     */
    JobDeliver findByJobIdAndStudentUserId(Long jobId, Long studentUserId);
    
    /**
     * 新增投递记录
     */
    int insert(JobDeliver jobDeliver);
    
    /**
     * 更新投递记录
     */
    int update(JobDeliver jobDeliver);
    
    /**
     * 更新投递状态
     */
    int updateDeliverStatus(Long id, Integer deliverStatus, String hrRemark);
    
    /**
     * 删除投递记录
     */
    int deleteById(Long id);

    /**
     * 分页查询学生的投递记录
     */
    List<JobDeliver> findByStudentUserIdPage(@Param("studentUserId") Long studentUserId,
                                             @Param("offset") int offset,
                                             @Param("pageSize") int pageSize);

    /**
     * 统计学生的投递记录数量
     */
    int countByStudentUserId(@Param("studentUserId") Long studentUserId);

    /**
     * 分页查询企业的投递记录（支持按岗位、状态筛选）
     */
    List<JobDeliver> findByEnterpriseIdPage(@Param("enterpriseId") Long enterpriseId,
                                            @Param("jobId") Long jobId,
                                            @Param("deliverStatus") Integer deliverStatus,
                                            @Param("offset") int offset,
                                            @Param("pageSize") int pageSize);

    /**
     * 统计企业的投递记录数量
     */
    int countByEnterpriseId(@Param("enterpriseId") Long enterpriseId,
                            @Param("jobId") Long jobId,
                            @Param("deliverStatus") Integer deliverStatus);

    /**
     * 统计已就业学生数（deliverStatus=4已录用 或 5已报到）
     */
    int countEmployedStudents();

    /**
     * 分页查询教师发布岗位的投递记录（联表查询学生和简历信息）
     */
    List<com.example.pojo.dto.DeliverDetailDTO> findByTeacherUserIdPage(@Param("teacherUserId") Long teacherUserId,
                                                                       @Param("jobId") Long jobId,
                                                                       @Param("deliverStatus") Integer deliverStatus,
                                                                       @Param("offset") int offset,
                                                                       @Param("pageSize") int pageSize);

    /**
     * 统计教师发布岗位的投递记录数量
     */
    int countByTeacherUserId(@Param("teacherUserId") Long teacherUserId,
                             @Param("jobId") Long jobId,
                             @Param("deliverStatus") Integer deliverStatus);

    /**
     * 分页查询企业的投递记录（联表查询学生和简历信息）
     */
    List<com.example.pojo.dto.DeliverDetailDTO> findByEnterpriseIdPageWithDetail(@Param("enterpriseId") Long enterpriseId,
                                                                                @Param("jobId") Long jobId,
                                                                                @Param("deliverStatus") Integer deliverStatus,
                                                                                @Param("offset") int offset,
                                                                                @Param("pageSize") int pageSize);

    /**
     * 统计薪资分布
     */
    List<java.util.Map<String, Object>> findSalaryStats();
}
