package com.example.service.impl;

import com.example.mapper.JobDeliverMapper;
import com.example.mapper.RecruitJobMapper;
import com.example.mapper.StudentResumeMapper;
import com.example.mapper.EnterpriseMapper;
import com.example.mapper.SysUserMapper;
import com.example.pojo.JobDeliver;
import com.example.pojo.RecruitJob;
import com.example.pojo.StudentResume;
import com.example.pojo.Enterprise;
import com.example.pojo.SysNotice;
import com.example.pojo.SysUser;
import com.example.pojo.InterviewNotice;
import com.example.service.JobDeliverService;
import com.example.service.SysNoticeService;
import com.example.service.InterviewNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 岗位投递服务实现类
 */
@Service
public class JobDeliverServiceImpl implements JobDeliverService {
    
    @Autowired
    private JobDeliverMapper jobDeliverMapper;
    
    @Autowired
    private RecruitJobMapper recruitJobMapper;
    
    @Autowired
    private StudentResumeMapper studentResumeMapper;
    
    @Autowired
    private EnterpriseMapper enterpriseMapper;
    
    @Autowired
    private SysUserMapper sysUserMapper;
    
    @Autowired
    private SysNoticeService sysNoticeService;
    
    @Autowired
    private InterviewNoticeService interviewNoticeService;
    
    @Override
    public JobDeliver findById(Long id) {
        return jobDeliverMapper.findById(id);
    }
    
    @Override
    public List<JobDeliver> findByJobId(Long jobId) {
        return jobDeliverMapper.findByJobId(jobId);
    }
    
    @Override
    public List<JobDeliver> findByStudentUserId(Long studentUserId) {
        return jobDeliverMapper.findByStudentUserId(studentUserId);
    }
    
    @Override
    public List<JobDeliver> findByEnterpriseId(Long enterpriseId) {
        return jobDeliverMapper.findByEnterpriseId(enterpriseId);
    }
    
    @Override
    public List<JobDeliver> findByDeliverStatus(Integer deliverStatus) {
        return jobDeliverMapper.findByDeliverStatus(deliverStatus);
    }
    
    @Override
    public List<JobDeliver> findByTrackId(String trackId) {
        return jobDeliverMapper.findByTrackId(trackId);
    }
    
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public int deliverJob(Long jobId, Long studentUserId, Long resumeId) {
        // 检查是否已投递
        if (hasDelivered(jobId, studentUserId)) {
            return -1;
        }
        
        // 获取岗位信息
        RecruitJob job = recruitJobMapper.findById(jobId);
        if (job == null) {
            return 0;
        }
        
        // 创建投递记录
        JobDeliver jobDeliver = new JobDeliver();
        jobDeliver.setTrackId(job.getTrackId());
        jobDeliver.setJobId(jobId);
        jobDeliver.setEnterpriseId(job.getEnterpriseId());
        jobDeliver.setStudentUserId(studentUserId);
        jobDeliver.setResumeId(resumeId);
        jobDeliver.setDeliverStatus(0);
        
        int result = jobDeliverMapper.insert(jobDeliver);
        
        // 更新岗位投递人数
        if (result > 0) {
            recruitJobMapper.updateDeliverCount(jobId);
            
            // 更新学生求职状态为投递中
            studentResumeMapper.updateJobStatus(studentUserId, 1);
            
            // 发送通知给HR或教师
            if (job.getEnterpriseId() != null) {
                // HR发布的岗位，通知HR用户
                List<SysUser> hrUsers = sysUserMapper.findByEnterpriseId(job.getEnterpriseId().intValue());
                for (SysUser hrUser : hrUsers) {
                    SysNotice notice = new SysNotice();
                    notice.setReceiveUserId(hrUser.getId());
                    notice.setTitle("新的简历投递");
                    notice.setContent("您发布的岗位【" + job.getJobName() + "】收到了新的简历投递，请及时查看。");
                    notice.setNoticeType(1);
                    notice.setRelatedId(jobId);
                    sysNoticeService.sendNotice(notice);
                }
            }
            
            if (job.getTeacherUserId() != null) {
                // 教师发布的岗位，通知教师
                SysNotice notice = new SysNotice();
                notice.setReceiveUserId(job.getTeacherUserId());
                notice.setTitle("新的简历投递");
                notice.setContent("您发布的岗位【" + job.getJobName() + "】收到了新的简历投递，请及时查看。");
                notice.setNoticeType(1);
                notice.setRelatedId(jobId);
                sysNoticeService.sendNotice(notice);
            }
            
            // 发送投递成功通知给学生
            SysNotice studentNotice = new SysNotice();
            studentNotice.setReceiveUserId(studentUserId);
            studentNotice.setTitle("投递成功");
            studentNotice.setContent("您已成功投递岗位【" + job.getJobName() + "】，请等待HR或教师的回复。");
            studentNotice.setNoticeType(1);
            studentNotice.setRelatedId(jobId);
            sysNoticeService.sendNotice(studentNotice);
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public int updateDeliverStatus(Long id, Integer deliverStatus, String hrRemark) {
        JobDeliver deliver = jobDeliverMapper.findById(id);
        if (deliver == null) {
            return 0;
        }
        
        RecruitJob job = recruitJobMapper.findById(deliver.getJobId());
        if (job == null) {
            return 0;
        }
        
        int result = jobDeliverMapper.updateDeliverStatus(id, deliverStatus, hrRemark);
        
        if (result > 0) {
            if (deliverStatus == 4) {
                studentResumeMapper.updateJobStatus(deliver.getStudentUserId(), 2);
            } else if (deliverStatus == 5) {
                studentResumeMapper.updateJobStatus(deliver.getStudentUserId(), 3);
            }
            
            
            
            String statusLabel = getStatusLabel(deliverStatus);
            SysNotice studentNotice = new SysNotice();
            studentNotice.setReceiveUserId(deliver.getStudentUserId());
            studentNotice.setTitle("您的投递状态已更新");
            studentNotice.setContent("您投递的岗位【" + job.getJobName() + "】状态已更新为" + statusLabel + "。");
            studentNotice.setNoticeType(2);
            studentNotice.setRelatedId(id);
            sysNoticeService.sendNotice(studentNotice);
            
            if (job.getEnterpriseId() != null && job.getTeacherUserId() != null) {
                List<SysUser> hrUsers = sysUserMapper.findByEnterpriseId(job.getEnterpriseId().intValue());
                for (SysUser hrUser : hrUsers) {
                    SysNotice notice = new SysNotice();
                    notice.setReceiveUserId(hrUser.getId());
                    notice.setTitle("投递状态已变更");
                    notice.setContent("岗位【" + job.getJobName() + "】的投递状态已更新为" + statusLabel + "。");
                    notice.setNoticeType(1);
                    notice.setRelatedId(id);
                    sysNoticeService.sendNotice(notice);
                }
                
                SysNotice teacherNotice = new SysNotice();
                teacherNotice.setReceiveUserId(job.getTeacherUserId());
                teacherNotice.setTitle("投递状态已变更");
                teacherNotice.setContent("岗位【" + job.getJobName() + "】的投递状态已更新为" + statusLabel + "。");
                teacherNotice.setNoticeType(1);
                teacherNotice.setRelatedId(id);
                sysNoticeService.sendNotice(teacherNotice);
            }
        }
        
        return result;
    }
    
    private String getStatusLabel(Integer status) {
        switch (status) {
            case 0: return "待处理";
            case 1: return "待面试";
            case 2: return "面试完成";
            case 3: return "不合适";
            case 4: return "已录用";
            case 5: return "已报到";
            default: return "未知";
        }
    }
    
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public int markAsInterview(Long id, String hrRemark) {
        return updateDeliverStatus(id, 1, hrRemark);
    }
    
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public int markAsRejected(Long id, String hrRemark) {
        return updateDeliverStatus(id, 3, hrRemark);
    }
    
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public int markAsHired(Long id, String hrRemark) {
        return updateDeliverStatus(id, 4, hrRemark);
    }
    
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public int markAsReported(Long id, String hrRemark) {
        return updateDeliverStatus(id, 5, hrRemark);
    }
    
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public int withdrawDeliver(Long id) {
        JobDeliver deliver = jobDeliverMapper.findById(id);
        if (deliver == null) {
            return 0;
        }
        
        RecruitJob job = recruitJobMapper.findById(deliver.getJobId());
        if (job == null) {
            return 0;
        }
        
        int result = jobDeliverMapper.deleteById(id);
        
        if (result > 0) {
            // 通知HR用户
            if (job.getEnterpriseId() != null) {
                List<SysUser> hrUsers = sysUserMapper.findByEnterpriseId(job.getEnterpriseId().intValue());
                for (SysUser hrUser : hrUsers) {
                    SysNotice notice = new SysNotice();
                    notice.setReceiveUserId(hrUser.getId());
                    notice.setTitle("投递已撤回");
                    notice.setContent("岗位【" + job.getJobName() + "】的一份投递已被学生撤回。");
                    notice.setNoticeType(1);
                    notice.setRelatedId(job.getId());
                    sysNoticeService.sendNotice(notice);
                }
            }
            
            // 通知教师用户
            if (job.getTeacherUserId() != null) {
                SysNotice notice = new SysNotice();
                notice.setReceiveUserId(job.getTeacherUserId());
                notice.setTitle("投递已撤回");
                notice.setContent("岗位【" + job.getJobName() + "】的一份投递已被学生撤回。");
                notice.setNoticeType(1);
                notice.setRelatedId(job.getId());
                sysNoticeService.sendNotice(notice);
            }
        }
        
        return result;
    }
    
    @Override
    public boolean hasDelivered(Long jobId, Long studentUserId) {
        JobDeliver deliver = jobDeliverMapper.findByJobIdAndStudentUserId(jobId, studentUserId);
        return deliver != null;
    }

    @Override
    public List<JobDeliver> findByStudentUserIdPage(Long studentUserId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return jobDeliverMapper.findByStudentUserIdPage(studentUserId, offset, pageSize);
    }

    @Override
    public int countByStudentUserId(Long studentUserId) {
        return jobDeliverMapper.countByStudentUserId(studentUserId);
    }

    @Override
    public List<JobDeliver> findByEnterpriseIdPage(Long enterpriseId, Long jobId, Integer deliverStatus, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return jobDeliverMapper.findByEnterpriseIdPage(enterpriseId, jobId, deliverStatus, offset, pageSize);
    }

    @Override
    public int countByEnterpriseId(Long enterpriseId, Long jobId, Integer deliverStatus) {
        return jobDeliverMapper.countByEnterpriseId(enterpriseId, jobId, deliverStatus);
    }

    @Override
    public int countEmployedStudents() {
        return jobDeliverMapper.countEmployedStudents();
    }

    @Override
    public List<com.example.pojo.dto.DeliverDetailDTO> findByTeacherUserIdPage(Long teacherUserId, Long jobId, Integer deliverStatus, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return jobDeliverMapper.findByTeacherUserIdPage(teacherUserId, jobId, deliverStatus, offset, pageSize);
    }

    @Override
    public int countByTeacherUserId(Long teacherUserId, Long jobId, Integer deliverStatus) {
        return jobDeliverMapper.countByTeacherUserId(teacherUserId, jobId, deliverStatus);
    }

    @Override
    public List<com.example.pojo.dto.DeliverDetailDTO> findByEnterpriseIdPageWithDetail(Long enterpriseId, Long jobId, Integer deliverStatus, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return jobDeliverMapper.findByEnterpriseIdPageWithDetail(enterpriseId, jobId, deliverStatus, offset, pageSize);
    }

    @Override
    public List<java.util.Map<String, Object>> findSalaryStats() {
        return jobDeliverMapper.findSalaryStats();
    }
}
