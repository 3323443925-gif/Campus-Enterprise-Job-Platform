package com.example.mapper;

import com.example.pojo.StudentResume;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 学生简历表Mapper接口
 */
@Mapper
public interface StudentResumeMapper {
    
    /**
     * 查询所有简历
     */
    List<StudentResume> findAll();
    
    /**
     * 根据ID查询简历
     */
    StudentResume findById(Long id);
    
    /**
     * 根据学生用户ID查询简历
     */
    StudentResume findByStudentUserId(Long studentUserId);
    
    /**
     * 根据专业ID查询简历
     */
    List<StudentResume> findByMajorId(Long majorId);
    
    /**
     * 根据求职状态查询
     */
    List<StudentResume> findByJobStatus(Integer jobStatus);
    
    /**
     * 新增简历
     */
    int insert(StudentResume studentResume);
    
    /**
     * 更新简历
     */
    int update(StudentResume studentResume);
    
    /**
     * 删除简历
     */
    int deleteById(Long id);
    
    /**
     * 更新求职状态
     */
    int updateJobStatus(Long studentUserId, Integer jobStatus);
}
