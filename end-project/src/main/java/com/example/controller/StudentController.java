package com.example.controller;

import com.example.pojo.InterviewNotice;
import com.example.pojo.JobDeliver;
import com.example.pojo.RecruitJob;
import com.example.pojo.Result;
import com.example.pojo.StudentResume;
import com.example.pojo.Enterprise;
import com.example.service.InterviewNoticeService;
import com.example.service.JobDeliverService;
import com.example.service.RecruitJobService;
import com.example.service.StudentResumeService;
import com.example.service.EnterpriseService;
import com.example.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学生端核心接口控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private RecruitJobService recruitJobService;

    @Autowired
    private StudentResumeService studentResumeService;

    @Autowired
    private JobDeliverService jobDeliverService;

    @Autowired
    private InterviewNoticeService interviewNoticeService;
    
    @Autowired
    private EnterpriseService enterpriseService;

    // ==================== 岗位浏览接口 ====================

    /**
     * 已发布岗位列表（支持按薪资、地点、关键词筛选）
     */
    @GetMapping("/job/page")
    public Result jobPage(@RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer pageSize,
                          @RequestParam(required = false) Integer salaryMin,
                          @RequestParam(required = false) Integer salaryMax,
                          @RequestParam(required = false) String workAddress,
                          @RequestParam(required = false) String keyword) {
        try {
            List<RecruitJob> jobs = recruitJobService.findPublishedJobsByPage(
                    salaryMin, salaryMax, workAddress, keyword, page, pageSize);
            int total = recruitJobService.countPublishedJobs(salaryMin, salaryMax, workAddress, keyword);

            Map<String, Object> data = new HashMap<>();
            data.put("list", jobs);
            data.put("total", total);
            data.put("page", page);
            data.put("pageSize", pageSize);
            data.put("totalPages", (total + pageSize - 1) / pageSize);

            log.info("查询岗位列表成功: page={}, pageSize={}, total={}", page, pageSize, total);
            return Result.success(data);
        } catch (Exception e) {
            log.error("查询岗位列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 岗位详情（浏览量自动 + 1）
     */
    @GetMapping("/job/{id}")
    public Result jobDetail(@PathVariable Long id) {
        try {
            RecruitJob job = recruitJobService.findById(id);
            if (job == null) {
                return Result.error("岗位不存在");
            }

            // 浏览量自动 +1
            recruitJobService.updateViewCount(id);

            log.info("查看岗位详情: jobId={}, jobName={}", id, job.getJobName());
            return Result.success(job);
        } catch (Exception e) {
            log.error("查询岗位详情失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    // ==================== 简历管理接口 ====================

    /**
     * 获取我的简历详情
     */
    @GetMapping("/resume")
    public Result myResume(HttpServletRequest request) {
        try {
            Long studentUserId = getCurrentUserId(request);
            if (studentUserId == null) {
                return Result.error("未登录");
            }

            StudentResume resume = studentResumeService.findByStudentUserId(studentUserId);
            if (resume == null) {
                return Result.error("简历不存在，请先创建简历");
            }

            log.info("获取学生简历成功: studentUserId={}", studentUserId);
            return Result.success(resume);
        } catch (Exception e) {
            log.error("获取学生简历失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 保存 / 更新结构化简历
     */
    @PostMapping("/resume")
    public Result saveOrUpdateResume(@RequestBody StudentResume resume, HttpServletRequest request) {
        try {
            Long studentUserId = getCurrentUserId(request);
            if (studentUserId == null) {
                return Result.error("未登录");
            }

            // 设置学生用户ID
            resume.setStudentUserId(studentUserId);

            // 检查是否已存在简历
            StudentResume existingResume = studentResumeService.findByStudentUserId(studentUserId);
            int result;
            if (existingResume != null) {
                // 更新简历
                resume.setId(existingResume.getId());
                result = studentResumeService.updateResume(resume);
                log.info("更新学生简历成功: studentUserId={}", studentUserId);
            } else {
                // 创建简历
                result = studentResumeService.createResume(resume);
                log.info("创建学生简历成功: studentUserId={}", studentUserId);
            }

            if (result > 0) {
                return Result.success();
            }
            return Result.error("保存简历失败");
        } catch (Exception e) {
            log.error("保存简历失败", e);
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 更新结构化简历（PUT请求）
     */
    @PutMapping("/resume")
    public Result updateResume(@RequestBody StudentResume resume, HttpServletRequest request) {
        return saveOrUpdateResume(resume, request);
    }

    // ==================== 岗位投递接口 ====================

    /**
     * 投递岗位（校验不可重复投递）
     */
    @PostMapping("/deliver/{jobId}")
    public Result deliverJob(@PathVariable Long jobId, HttpServletRequest request) {
        try {
            Long studentUserId = getCurrentUserId(request);
            if (studentUserId == null) {
                return Result.error("未登录");
            }

            // 检查是否已投递
            if (jobDeliverService.hasDelivered(jobId, studentUserId)) {
                return Result.error("您已投递过该岗位，不能重复投递");
            }

            // 获取学生简历
            StudentResume resume = studentResumeService.findByStudentUserId(studentUserId);
            if (resume == null) {
                return Result.error("请先完善简历后再投递");
            }

            // 投递简历
            int result = jobDeliverService.deliverJob(jobId, studentUserId, resume.getId());
            if (result > 0) {
                log.info("投递岗位成功: jobId={}, studentUserId={}", jobId, studentUserId);
                return Result.success();
            } else if (result == -1) {
                return Result.error("您已投递过该岗位，不能重复投递");
            }
            return Result.error("投递失败");
        } catch (Exception e) {
            log.error("投递岗位失败", e);
            return Result.error("投递失败: " + e.getMessage());
        }
    }

    /**
     * 我的投递记录列表（带实时状态）
     */
    @GetMapping("/deliver/page")
    public Result deliverPage(@RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              HttpServletRequest request) {
        try {
            Long studentUserId = getCurrentUserId(request);
            if (studentUserId == null) {
                return Result.error("未登录");
            }

            List<JobDeliver> delivers = jobDeliverService.findByStudentUserIdPage(studentUserId, page, pageSize);
            int total = jobDeliverService.countByStudentUserId(studentUserId);

            Map<String, Object> data = new HashMap<>();
            data.put("list", delivers);
            data.put("total", total);
            data.put("page", page);
            data.put("pageSize", pageSize);
            data.put("totalPages", (total + pageSize - 1) / pageSize);

            log.info("查询投递记录成功: studentUserId={}, page={}, pageSize={}, total={}", 
                    studentUserId, page, pageSize, total);
            return Result.success(data);
        } catch (Exception e) {
            log.error("查询投递记录失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 投递详情
     */
    @GetMapping("/deliver/{id}")
    public Result deliverDetail(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long studentUserId = getCurrentUserId(request);
            if (studentUserId == null) {
                return Result.error("未登录");
            }

            JobDeliver deliver = jobDeliverService.findById(id);
            if (deliver == null) {
                return Result.error("投递记录不存在");
            }

            // 验证是否属于当前学生
            if (!deliver.getStudentUserId().equals(studentUserId)) {
                return Result.error("无权查看该投递记录");
            }

            log.info("查看投递详情: deliverId={}, studentUserId={}", id, studentUserId);
            return Result.success(deliver);
        } catch (Exception e) {
            log.error("查询投递详情失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    // ==================== 面试通知接口 ====================

    /**
     * 我的面试通知列表
     */
    @GetMapping("/interview/page")
    public Result interviewPage(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                HttpServletRequest request) {
        try {
            Long studentUserId = getCurrentUserId(request);
            if (studentUserId == null) {
                return Result.error("未登录");
            }

            List<InterviewNotice> interviews = interviewNoticeService.findByStudentUserIdPage(studentUserId, page, pageSize);
            int total = interviewNoticeService.countByStudentUserId(studentUserId);

            List<Map<String, Object>> resultList = new java.util.ArrayList<>();
            for (InterviewNotice notice : interviews) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", notice.getId());
                item.put("deliverId", notice.getDeliverId());
                item.put("jobId", notice.getJobId());
                item.put("studentUserId", notice.getStudentUserId());
                item.put("hrUserId", notice.getHrUserId());
                item.put("interviewTime", notice.getInterviewTime());
                item.put("interviewAddress", notice.getInterviewAddress());
                item.put("interviewContent", notice.getInterviewContent());
                item.put("isRead", notice.getIsRead());
                item.put("interviewStatus", notice.getInterviewStatus() != null ? notice.getInterviewStatus() : 0);
                item.put("createTime", notice.getCreateTime());

                RecruitJob job = recruitJobService.findById(notice.getJobId());
                if (job != null) {
                    item.put("jobName", job.getJobName());
                    Enterprise enterprise = enterpriseService.findById(job.getEnterpriseId());
                    if (enterprise != null) {
                        item.put("companyName", enterprise.getCompanyName());
                    }
                }

                resultList.add(item);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("list", resultList);
            data.put("total", total);
            data.put("page", page);
            data.put("pageSize", pageSize);
            data.put("totalPages", (total + pageSize - 1) / pageSize);

            log.info("查询面试通知成功: studentUserId={}, page={}, pageSize={}, total={}", 
                    studentUserId, page, pageSize, total);
            return Result.success(data);
        } catch (Exception e) {
            log.error("查询面试通知失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 接受面试
     */
    @PutMapping("/interview/{id}/accept")
    public Result acceptInterview(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long studentUserId = getCurrentUserId(request);
            if (studentUserId == null) {
                return Result.error("未登录");
            }

            InterviewNotice notice = interviewNoticeService.findById(id);
            if (notice == null) {
                return Result.error("面试通知不存在");
            }

            if (!notice.getStudentUserId().equals(studentUserId)) {
                return Result.error("无权操作该面试通知");
            }

            if (notice.getInterviewStatus() != null && notice.getInterviewStatus() != 0) {
                return Result.error("您已处理过该面试通知");
            }

            notice.setInterviewStatus(1);
            int result = interviewNoticeService.update(notice);
            if (result > 0) {
                log.info("学生接受面试: interviewId={}, studentUserId={}", id, studentUserId);
                return Result.success();
            }
            return Result.error("操作失败");
        } catch (Exception e) {
            log.error("接受面试失败", e);
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 拒绝面试
     */
    @PutMapping("/interview/{id}/reject")
    public Result rejectInterview(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long studentUserId = getCurrentUserId(request);
            if (studentUserId == null) {
                return Result.error("未登录");
            }

            InterviewNotice notice = interviewNoticeService.findById(id);
            if (notice == null) {
                return Result.error("面试通知不存在");
            }

            if (!notice.getStudentUserId().equals(studentUserId)) {
                return Result.error("无权操作该面试通知");
            }

            if (notice.getInterviewStatus() != null && notice.getInterviewStatus() != 0) {
                return Result.error("您已处理过该面试通知");
            }

            notice.setInterviewStatus(2);
            int result = interviewNoticeService.update(notice);
            if (result > 0) {
                log.info("学生拒绝面试: interviewId={}, studentUserId={}", id, studentUserId);
                return Result.success();
            }
            return Result.error("操作失败");
        } catch (Exception e) {
            log.error("拒绝面试失败", e);
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 从请求中获取当前登录用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            return null;
        }

        try {
            Claims claims = JwtUtils.parseToken(token);
            return Long.valueOf(claims.get("id").toString());
        } catch (Exception e) {
            log.error("解析token失败", e);
            return null;
        }
    }
}
