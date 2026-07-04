package com.example.service.impl;

import com.example.mapper.StudentResumeMapper;
import com.example.pojo.StudentResume;
import com.example.service.StudentResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 学生简历服务实现类
 */
@Service
public class StudentResumeServiceImpl implements StudentResumeService {
    
    @Autowired
    private StudentResumeMapper studentResumeMapper;
    
    @Override
    public StudentResume findById(Long id) {
        return studentResumeMapper.findById(id);
    }
    
    @Override
    public StudentResume findByStudentUserId(Long studentUserId) {
        return studentResumeMapper.findByStudentUserId(studentUserId);
    }
    
    @Override
    public List<StudentResume> findByMajorId(Long majorId) {
        return studentResumeMapper.findByMajorId(majorId);
    }
    
    @Override
    public List<StudentResume> findByJobStatus(Integer jobStatus) {
        return studentResumeMapper.findByJobStatus(jobStatus);
    }
    
    @Override
    public int createResume(StudentResume studentResume) {
        // 默认求职状态为未投递
        if (studentResume.getJobStatus() == null) {
            studentResume.setJobStatus(0);
        }
        return studentResumeMapper.insert(studentResume);
    }
    
    @Override
    public int updateResume(StudentResume studentResume) {
        return studentResumeMapper.update(studentResume);
    }
    
    @Override
    public int deleteById(Long id) {
        return studentResumeMapper.deleteById(id);
    }
    
    @Override
    public int updateJobStatus(Long studentUserId, Integer jobStatus) {
        return studentResumeMapper.updateJobStatus(studentUserId, jobStatus);
    }
    
    @Override
    public int uploadResumeFile(Long studentUserId, String filePath) {
        StudentResume resume = studentResumeMapper.findByStudentUserId(studentUserId);
        if (resume == null) {
            return 0;
        }
        resume.setResumeFile(filePath);
        return studentResumeMapper.update(resume);
    }
}
