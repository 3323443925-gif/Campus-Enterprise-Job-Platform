package com.example.controller;

import com.example.pojo.Enterprise;
import com.example.pojo.InterviewNotice;
import com.example.pojo.JobDeliver;
import com.example.pojo.RecruitJob;
import com.example.pojo.Result;
import com.example.pojo.StudentResume;
import com.example.pojo.SysUser;
import com.example.service.EnterpriseService;
import com.example.service.InterviewNoticeService;
import com.example.service.JobDeliverService;
import com.example.service.RecruitJobService;
import com.example.service.StudentResumeService;
import com.example.service.SysUserService;
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
 * 教师端核心接口控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @Autowired
    private RecruitJobService recruitJobService;

    @Autowired
    private JobDeliverService jobDeliverService;

    @Autowired
    private EnterpriseService enterpriseService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private StudentResumeService studentResumeService;

    @Autowired
    private InterviewNoticeService interviewNoticeService;

    // ==================== 岗位管理接口 ====================

    /**
     * 教师发布/管理的岗位列表（支持按状态筛选）
     */
    @GetMapping("/job/page")
    public Result jobPage(@RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer pageSize,
                          @RequestParam(required = false) Integer status,
                          HttpServletRequest request) {
        try {
            Long teacherUserId = getCurrentUserId(request);
            if (teacherUserId == null) {
                return Result.error("未登录");
            }

            List<RecruitJob> jobs = recruitJobService.findByTeacherUserIdPage(teacherUserId, status, page, pageSize);
            int total = recruitJobService.countByTeacherUserId(teacherUserId, status);

            Map<String, Object> data = new HashMap<>();
            data.put("list", jobs);
            data.put("total", total);
            data.put("page", page);
            data.put("pageSize", pageSize);
            data.put("totalPages", (total + pageSize - 1) / pageSize);

            log.info("查询教师岗位列表成功: teacherUserId={}, page={}, pageSize={}, total={}", 
                    teacherUserId, page, pageSize, total);
            return Result.success(data);
        } catch (Exception e) {
            log.error("查询教师岗位列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 新增岗位（草稿状态）
     */
    @PostMapping("/job")
    public Result addJob(@RequestBody RecruitJob recruitJob, HttpServletRequest request) {
        try {
            Long teacherUserId = getCurrentUserId(request);
            if (teacherUserId == null) {
                return Result.error("未登录");
            }

            recruitJob.setTeacherUserId(teacherUserId);
            recruitJob.setStatus(0); // 草稿状态

            int result = recruitJobService.insert(recruitJob);
            if (result > 0) {
                log.info("新增岗位成功: teacherUserId={}, jobName={}", teacherUserId, recruitJob.getJobName());
                return Result.success();
            }
            return Result.error("新增岗位失败");
        } catch (Exception e) {
            log.error("新增岗位失败", e);
            return Result.error("新增失败: " + e.getMessage());
        }
    }

    /**
     * 更新岗位信息
     */
    @PutMapping("/job")
    public Result updateJob(@RequestBody RecruitJob recruitJob, HttpServletRequest request) {
        try {
            Long teacherUserId = getCurrentUserId(request);
            if (teacherUserId == null) {
                return Result.error("未登录");
            }

            if (recruitJob.getId() == null) {
                return Result.error("岗位ID不能为空");
            }

            // 验证岗位是否属于当前教师
            RecruitJob existingJob = recruitJobService.findById(recruitJob.getId());
            if (existingJob == null) {
                return Result.error("岗位不存在");
            }
            if (!existingJob.getTeacherUserId().equals(teacherUserId)) {
                return Result.error("无权操作该岗位");
            }

            int result = recruitJobService.update(recruitJob);
            if (result > 0) {
                log.info("更新岗位成功: jobId={}", recruitJob.getId());
                return Result.success();
            }
            return Result.error("更新岗位失败");
        } catch (Exception e) {
            log.error("更新岗位失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 发布岗位（从草稿变为已发布）
     */
    @PutMapping("/job/{id}/publish")
    public Result publishJob(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long teacherUserId = getCurrentUserId(request);
            if (teacherUserId == null) {
                return Result.error("未登录");
            }

            RecruitJob job = recruitJobService.findById(id);
            if (job == null) {
                return Result.error("岗位不存在");
            }
            if (!job.getTeacherUserId().equals(teacherUserId)) {
                return Result.error("无权操作该岗位");
            }

            // 生成追踪ID
            if (job.getTrackId() == null || job.getTrackId().isEmpty()) {
                job.setTrackId(recruitJobService.generateTrackId());
            }
            job.setStatus(1); // 已发布

            int result = recruitJobService.update(job);
            if (result > 0) {
                log.info("发布岗位成功: jobId={}, teacherUserId={}", id, teacherUserId);
                return Result.success();
            }
            return Result.error("发布岗位失败");
        } catch (Exception e) {
            log.error("发布岗位失败", e);
            return Result.error("发布失败: " + e.getMessage());
        }
    }

    /**
     * 暂停招聘
     */
    @PutMapping("/job/{id}/pause")
    public Result pauseJob(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long teacherUserId = getCurrentUserId(request);
            if (teacherUserId == null) {
                return Result.error("未登录");
            }

            RecruitJob job = recruitJobService.findById(id);
            if (job == null) {
                return Result.error("岗位不存在");
            }
            if (!job.getTeacherUserId().equals(teacherUserId)) {
                return Result.error("无权操作该岗位");
            }

            int result = recruitJobService.pauseRecruit(id);
            if (result > 0) {
                log.info("暂停岗位成功: jobId={}", id);
                return Result.success();
            }
            return Result.error("暂停岗位失败");
        } catch (Exception e) {
            log.error("暂停岗位失败", e);
            return Result.error("暂停失败: " + e.getMessage());
        }
    }

    /**
     * 关闭招聘
     */
    @PutMapping("/job/{id}/close")
    public Result closeJob(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long teacherUserId = getCurrentUserId(request);
            if (teacherUserId == null) {
                return Result.error("未登录");
            }

            RecruitJob job = recruitJobService.findById(id);
            if (job == null) {
                return Result.error("岗位不存在");
            }
            if (!job.getTeacherUserId().equals(teacherUserId)) {
                return Result.error("无权操作该岗位");
            }

            int result = recruitJobService.closeRecruit(id);
            if (result > 0) {
                log.info("关闭岗位成功: jobId={}", id);
                return Result.success();
            }
            return Result.error("关闭岗位失败");
        } catch (Exception e) {
            log.error("关闭岗位失败", e);
            return Result.error("关闭失败: " + e.getMessage());
        }
    }

    /**
     * 删除岗位（逻辑删除，仅已关闭状态可删除）
     */
    @DeleteMapping("/job/{id}")
    public Result deleteJob(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long teacherUserId = getCurrentUserId(request);
            if (teacherUserId == null) {
                return Result.error("未登录");
            }

            RecruitJob job = recruitJobService.findById(id);
            if (job == null) {
                return Result.error("岗位不存在");
            }
            if (!job.getTeacherUserId().equals(teacherUserId)) {
                return Result.error("无权操作该岗位");
            }

            int result = recruitJobService.deleteById(id);
            if (result > 0) {
                log.info("删除岗位成功: jobId={}", id);
                return Result.success();
            }
            return Result.error("删除岗位失败");
        } catch (Exception e) {
            log.error("删除岗位失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    // ==================== 企业管理接口 ====================

    /**
     * 获取企业列表（所有企业，支持搜索筛选）
     */
    @GetMapping("/enterprise/page")
    public Result enterprisePage(@RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) String industry,
                                 HttpServletRequest request) {
        try {
            Long teacherUserId = getCurrentUserId(request);
            if (teacherUserId == null) {
                return Result.error("未登录");
            }

            List<Enterprise> enterprises = enterpriseService.findByCondition(keyword, industry);
            int total = enterprises.size();

            Map<String, Object> data = new HashMap<>();
            data.put("list", enterprises);
            data.put("total", total);
            data.put("page", page);
            data.put("pageSize", pageSize);

            log.info("查询企业列表成功: teacherUserId={}, keyword={}, industry={}, count={}", 
                    teacherUserId, keyword, industry, total);
            return Result.success(data);
        } catch (Exception e) {
            log.error("查询企业列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 新增企业（教师录入）
     */
    @PostMapping("/enterprise")
    public Result addEnterprise(@RequestBody Enterprise enterprise, HttpServletRequest request) {
        try {
            Long teacherUserId = getCurrentUserId(request);
            if (teacherUserId == null) {
                return Result.error("未登录");
            }

            enterprise.setCreateUserId(teacherUserId);

            int result = enterpriseService.insert(enterprise);
            if (result > 0) {
                log.info("新增企业成功: teacherUserId={}, companyName={}", teacherUserId, enterprise.getCompanyName());
                return Result.success();
            }
            return Result.error("新增企业失败");
        } catch (Exception e) {
            log.error("新增企业失败", e);
            return Result.error("新增失败: " + e.getMessage());
        }
    }

    /**
     * 更新企业信息（教师编辑）
     */
    @PutMapping("/enterprise")
    public Result updateEnterprise(@RequestBody Enterprise enterprise, HttpServletRequest request) {
        try {
            Long teacherUserId = getCurrentUserId(request);
            if (teacherUserId == null) {
                return Result.error("未登录");
            }

            if (enterprise.getId() == null) {
                return Result.error("企业ID不能为空");
            }

            Enterprise existingEnterprise = enterpriseService.findById(enterprise.getId());
            if (existingEnterprise == null) {
                return Result.error("企业不存在");
            }
            if (!existingEnterprise.getCreateUserId().equals(teacherUserId)) {
                return Result.error("无权操作该企业");
            }

            int result = enterpriseService.update(enterprise);
            if (result > 0) {
                log.info("更新企业成功: enterpriseId={}", enterprise.getId());
                return Result.success();
            }
            return Result.error("更新企业失败");
        } catch (Exception e) {
            log.error("更新企业失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    // ==================== 投递统计接口 ====================

    /**
     * 投递统计列表（查看学生投递情况）
     */
    @GetMapping("/deliver/page")
    public Result deliverPage(@RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(required = false) Long jobId,
                              @RequestParam(required = false) Integer deliverStatus,
                              HttpServletRequest request) {
        try {
            Long teacherUserId = getCurrentUserId(request);
            if (teacherUserId == null) {
                return Result.error("未登录");
            }

            List<com.example.pojo.dto.DeliverDetailDTO> deliverList = jobDeliverService.findByTeacherUserIdPage(teacherUserId, jobId, deliverStatus, page, pageSize);
            int total = jobDeliverService.countByTeacherUserId(teacherUserId, jobId, deliverStatus);

            Map<String, Object> data = new HashMap<>();
            data.put("list", deliverList);
            data.put("total", total);

            log.info("查询投递统计成功: teacherUserId={}, deliverCount={}", teacherUserId, deliverList.size());
            return Result.success(data);
        } catch (Exception e) {
            log.error("查询投递统计失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 更新投递状态（教师端）
     */
    @PutMapping("/deliver/{id}/status")
    public Result updateDeliverStatus(@PathVariable Long id,
                                      @RequestBody Map<String, Object> requestData,
                                      HttpServletRequest request) {
        try {
            Long teacherUserId = getCurrentUserId(request);
            if (teacherUserId == null) {
                return Result.error("未登录");
            }

            JobDeliver deliver = jobDeliverService.findById(id);
            if (deliver == null) {
                return Result.error("投递记录不存在");
            }

            RecruitJob job = recruitJobService.findById(deliver.getJobId());
            if (job == null || !teacherUserId.equals(job.getTeacherUserId())) {
                return Result.error("无权操作该投递记录");
            }

            Object statusObj = requestData.get("deliverStatus");
            Integer deliverStatus = null;
            if (statusObj != null) {
                deliverStatus = Integer.valueOf(statusObj.toString());
            }
            String hrRemark = (String) requestData.get("hrRemark");

            if (deliverStatus == null) {
                return Result.error("投递状态不能为空");
            }

            int result = jobDeliverService.updateDeliverStatus(id, deliverStatus, hrRemark);
            if (result > 0) {
                log.info("更新投递状态成功: deliverId={}, deliverStatus={}, teacherUserId={}", id, deliverStatus, teacherUserId);
                return Result.success();
            }
            return Result.error("更新状态失败");
        } catch (Exception e) {
            log.error("更新投递状态失败", e);
            return Result.error("更新失败: " + e.getMessage());
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