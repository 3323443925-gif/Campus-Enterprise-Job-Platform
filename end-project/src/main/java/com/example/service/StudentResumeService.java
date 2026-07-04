package com.example.service;

import com.example.pojo.StudentResume;
import java.util.List;

/**
 * 学生简历服务接口
 */
public interface StudentResumeService {
    
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
     * 创建简历
     */
    int createResume(StudentResume studentResume);
    
    /**
     * 更新简历
     */
    int updateResume(StudentResume studentResume);
    
    /**
     * 删除简历
     */
    int deleteById(Long id);
    
    /**
     * 更新求职状态
     */
    int updateJobStatus(Long studentUserId, Integer jobStatus);
    
    /**
     * 上传PDF简历
     */
    int uploadResumeFile(Long studentUserId, String filePath);
}
