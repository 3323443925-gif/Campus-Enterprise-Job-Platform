package com.example.service.impl;

import com.example.mapper.RecruitJobMapper;
import com.example.pojo.RecruitJob;
import com.example.service.RecruitJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 招聘岗位服务实现类
 */
@Service
public class RecruitJobServiceImpl implements RecruitJobService {
    
    @Autowired
    private RecruitJobMapper recruitJobMapper;
    
    @Override
    public List<RecruitJob> findAll() {
        return recruitJobMapper.findAll();
    }
    
    @Override
    public RecruitJob findById(Long id) {
        return recruitJobMapper.findById(id);
    }
    
    @Override
    public RecruitJob findByTrackId(String trackId) {
        return recruitJobMapper.findByTrackId(trackId);
    }
    
    @Override
    public List<RecruitJob> findByEnterpriseId(Long enterpriseId) {
        return recruitJobMapper.findByEnterpriseId(enterpriseId);
    }
    
    @Override
    public List<RecruitJob> findByTeacherUserId(Long teacherUserId) {
        return recruitJobMapper.findByTeacherUserId(teacherUserId);
    }
    
    @Override
    public List<RecruitJob> findByStatus(Integer status) {
        return recruitJobMapper.findByStatus(status);
    }
    
    @Override
    public List<RecruitJob> findByJobCategory(String jobCategory) {
        return recruitJobMapper.findByJobCategory(jobCategory);
    }
    
    @Override
    public List<RecruitJob> findPublishedJobs() {
        return recruitJobMapper.findPublishedJobs();
    }
    
    @Override
    public int insert(RecruitJob recruitJob) {
        // 生成追踪ID
        if (recruitJob.getTrackId() == null || recruitJob.getTrackId().isEmpty()) {
            recruitJob.setTrackId(generateTrackId());
        }
        return recruitJobMapper.insert(recruitJob);
    }

    @Override
    public int publishJob(RecruitJob recruitJob) {
        // 生成追踪ID
        if (recruitJob.getTrackId() == null || recruitJob.getTrackId().isEmpty()) {
            recruitJob.setTrackId(generateTrackId());
        }
        // 设置状态为已发布
        recruitJob.setStatus(1);
        return recruitJobMapper.insert(recruitJob);
    }
    
    @Override
    public int update(RecruitJob recruitJob) {
        return recruitJobMapper.update(recruitJob);
    }
    
    @Override
    public int deleteById(Long id) {
        return recruitJobMapper.deleteById(id);
    }
    
    @Override
    public int pauseRecruit(Long id) {
        RecruitJob job = new RecruitJob();
        job.setId(id);
        job.setStatus(2); // 暂停招聘
        return recruitJobMapper.update(job);
    }
    
    @Override
    public int closeRecruit(Long id) {
        RecruitJob job = new RecruitJob();
        job.setId(id);
        job.setStatus(3); // 已关闭
        return recruitJobMapper.update(job);
    }
    
    @Override
    public int updateViewCount(Long id) {
        return recruitJobMapper.updateViewCount(id);
    }
    
    @Override
    public int updateDeliverCount(Long id) {
        return recruitJobMapper.updateDeliverCount(id);
    }
    
    @Override
    public String generateTrackId() {
        // 格式：JOB_年月日_随机串
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "JOB_" + dateStr + "_" + randomStr;
    }

    @Override
    public List<RecruitJob> findPublishedJobsByPage(Integer salaryMin, Integer salaryMax, 
                                                     String workAddress, String keyword, 
                                                     int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return recruitJobMapper.findPublishedJobsByPage(salaryMin, salaryMax, workAddress, keyword, offset, pageSize);
    }

    @Override
    public int countPublishedJobs(Integer salaryMin, Integer salaryMax, 
                                   String workAddress, String keyword) {
        return recruitJobMapper.countPublishedJobs(salaryMin, salaryMax, workAddress, keyword);
    }

    @Override
    public List<RecruitJob> findByEnterpriseIdPage(Long enterpriseId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return recruitJobMapper.findByEnterpriseIdPage(enterpriseId, offset, pageSize);
    }

    @Override
    public int countByEnterpriseId(Long enterpriseId) {
        return recruitJobMapper.countByEnterpriseId(enterpriseId);
    }

    @Override
    public int countTotalJobs() {
        return recruitJobMapper.countTotalJobs();
    }

    @Override
    public List<RecruitJob> findByTeacherUserIdPage(Long teacherUserId, Integer status, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return recruitJobMapper.findByTeacherUserIdPage(teacherUserId, status, offset, pageSize);
    }

    @Override
    public int countByTeacherUserId(Long teacherUserId, Integer status) {
        return recruitJobMapper.countByTeacherUserId(teacherUserId, status);
    }

    @Override
    public List<java.util.Map<String, Object>> findJobCategoryStats() {
        return recruitJobMapper.findJobCategoryStats();
    }
}
