package com.example.controller;

import com.example.pojo.Enterprise;
import com.example.pojo.EnterpriseAudit;
import com.example.pojo.InterviewNotice;
import com.example.pojo.JobDeliver;
import com.example.pojo.RecruitJob;
import com.example.pojo.Result;
import com.example.pojo.StudentResume;
import com.example.pojo.SysUser;
import com.example.mapper.JobDeliverMapper;
import com.example.service.EnterpriseAuditService;
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
 * 企业 HR 端核心接口控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/hr")
public class HrController {

    @Autowired
    private EnterpriseAuditService enterpriseAuditService;

    @Autowired
    private EnterpriseService enterpriseService;

    @Autowired
    private RecruitJobService recruitJobService;

    @Autowired
    private JobDeliverService jobDeliverService;

    @Autowired
    private JobDeliverMapper jobDeliverMapper;

    @Autowired
    private InterviewNoticeService interviewNoticeService;

    @Autowired
    private StudentResumeService studentResumeService;

    @Autowired
    private SysUserService sysUserService;

    // ==================== 企业认证接口 ====================

    /**
     * 提交企业认证申请（营业执照、企业信息）
     */
    @PostMapping("/audit/submit")
    public Result submitAudit(@RequestBody Map<String, Object> requestData, HttpServletRequest request) {
        try {
            Long hrUserId = getCurrentUserId(request);
            if (hrUserId == null) {
                return Result.error("未登录");
            }

            String companyName = (String) requestData.get("companyName");
            String unifiedSocialCode = (String) requestData.get("unifiedSocialCode");
            String address = (String) requestData.get("address");
            String industry = (String) requestData.get("industry");
            String contactName = (String) requestData.get("contactName");
            String contactPhone = (String) requestData.get("contactPhone");
            String contactEmail = (String) requestData.get("contactEmail");
            String businessLicenseImg = (String) requestData.get("businessLicenseImg");
            
            Integer cooperationLevel = null;
            Object coopLevelObj = requestData.get("cooperationLevel");
            if (coopLevelObj != null) {
                cooperationLevel = Integer.valueOf(coopLevelObj.toString());
            }

            if (businessLicenseImg == null || businessLicenseImg.isEmpty()) {
                return Result.error("营业执照图片不能为空");
            }

            SysUser hrUser = sysUserService.findById(hrUserId);
            if (hrUser == null) {
                return Result.error("用户不存在");
            }

            Long enterpriseId = hrUser.getEnterpriseId() != null ? hrUser.getEnterpriseId().longValue() : null;
            
            if (enterpriseId == null) {
                Enterprise enterprise = new Enterprise();
                enterprise.setCompanyName(companyName);
                enterprise.setUnifiedSocialCode(unifiedSocialCode);
                enterprise.setAddress(address);
                enterprise.setIndustry(industry);
                enterprise.setContactName(contactName);
                enterprise.setContactPhone(contactPhone);
                enterprise.setContactEmail(contactEmail);
                enterprise.setCooperationLevel(cooperationLevel != null ? cooperationLevel : 1);
                enterprise.setEnterpriseStatus(0);
                enterprise.setCreateUserId(hrUserId);
                
                int result = enterpriseService.insert(enterprise);
                if (result <= 0) {
                    return Result.error("创建企业信息失败");
                }
                
                enterpriseId = enterprise.getId();
                
                hrUser.setEnterpriseId(enterpriseId.intValue());
                sysUserService.update(hrUser);
            } else {
                Enterprise enterprise = enterpriseService.findById(enterpriseId);
                if (enterprise != null) {
                    enterprise.setCompanyName(companyName);
                    enterprise.setUnifiedSocialCode(unifiedSocialCode);
                    enterprise.setAddress(address);
                    enterprise.setIndustry(industry);
                    enterprise.setContactName(contactName);
                    enterprise.setContactPhone(contactPhone);
                    enterprise.setContactEmail(contactEmail);
                    enterprise.setEnterpriseStatus(0);
                    if (cooperationLevel != null) {
                        enterprise.setCooperationLevel(cooperationLevel);
                    }
                    enterpriseService.update(enterprise);
                } else {
                    Enterprise newEnterprise = new Enterprise();
                    newEnterprise.setCompanyName(companyName);
                    newEnterprise.setUnifiedSocialCode(unifiedSocialCode);
                    newEnterprise.setAddress(address);
                    newEnterprise.setIndustry(industry);
                    newEnterprise.setContactName(contactName);
                    newEnterprise.setContactPhone(contactPhone);
                    newEnterprise.setContactEmail(contactEmail);
                    newEnterprise.setCooperationLevel(cooperationLevel != null ? cooperationLevel : 1);
                    newEnterprise.setEnterpriseStatus(0);
                    newEnterprise.setCreateUserId(hrUserId);
                    
                    int result = enterpriseService.insert(newEnterprise);
                    if (result <= 0) {
                        return Result.error("创建企业信息失败");
                    }
                    
                    enterpriseId = newEnterprise.getId();
                    
                    hrUser.setEnterpriseId(enterpriseId.intValue());
                    sysUserService.update(hrUser);
                }
            }

            EnterpriseAudit audit = new EnterpriseAudit();
            audit.setEnterpriseId(enterpriseId);
            audit.setHrUserId(hrUserId);
            audit.setBusinessLicenseImg(businessLicenseImg);
            audit.setAuditStatus(0);

            int auditResult = enterpriseAuditService.submitAudit(audit);
            if (auditResult > 0) {
                log.info("提交企业认证申请成功: hrUserId={}, enterpriseId={}", hrUserId, enterpriseId);
                return Result.success();
            }
            return Result.error("提交认证申请失败");
        } catch (Exception e) {
            log.error("提交企业认证申请失败", e);
            return Result.error("提交失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前HR的企业信息
     */
    @GetMapping("/enterprise")
    public Result getEnterpriseInfo(HttpServletRequest request) {
        try {
            Long hrUserId = getCurrentUserId(request);
            if (hrUserId == null) {
                return Result.error("未登录");
            }

            Long enterpriseId = getEnterpriseId(hrUserId);
            if (enterpriseId == null) {
                return Result.error("企业信息不存在");
            }

            Enterprise enterprise = enterpriseService.findById(enterpriseId);
            if (enterprise == null) {
                return Result.error("企业信息不存在");
            }

            log.info("获取企业信息成功: enterpriseId={}", enterpriseId);
            return Result.success(enterprise);
        } catch (Exception e) {
            log.error("获取企业信息失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 更新企业信息（认证通过后修改）
     */
    @PutMapping("/enterprise")
    public Result updateEnterprise(@RequestBody Map<String, Object> requestData, HttpServletRequest request) {
        try {
            Long hrUserId = getCurrentUserId(request);
            if (hrUserId == null) {
                return Result.error("未登录");
            }

            Long enterpriseId = getEnterpriseId(hrUserId);
            if (enterpriseId == null) {
                return Result.error("企业信息不存在");
            }

            Enterprise enterprise = enterpriseService.findById(enterpriseId);
            if (enterprise == null) {
                return Result.error("企业信息不存在");
            }

            String companyName = (String) requestData.get("companyName");
            String unifiedSocialCode = (String) requestData.get("unifiedSocialCode");
            String address = (String) requestData.get("address");
            String industry = (String) requestData.get("industry");
            String contactName = (String) requestData.get("contactName");
            String contactPhone = (String) requestData.get("contactPhone");
            String contactEmail = (String) requestData.get("contactEmail");
            
            Integer cooperationLevel = null;
            Object coopLevelObj = requestData.get("cooperationLevel");
            if (coopLevelObj != null) {
                cooperationLevel = Integer.valueOf(coopLevelObj.toString());
            }

            if (companyName != null) enterprise.setCompanyName(companyName);
            if (unifiedSocialCode != null) enterprise.setUnifiedSocialCode(unifiedSocialCode);
            if (address != null) enterprise.setAddress(address);
            if (industry != null) enterprise.setIndustry(industry);
            if (contactName != null) enterprise.setContactName(contactName);
            if (contactPhone != null) enterprise.setContactPhone(contactPhone);
            if (contactEmail != null) enterprise.setContactEmail(contactEmail);
            if (cooperationLevel != null) enterprise.setCooperationLevel(cooperationLevel);

            int result = enterpriseService.update(enterprise);
            if (result > 0) {
                log.info("更新企业信息成功: enterpriseId={}", enterpriseId);
                return Result.success();
            }
            return Result.error("更新企业信息失败");
        } catch (Exception e) {
            log.error("更新企业信息失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 查询我的企业认证状态
     */
    @GetMapping("/audit/status")
    public Result auditStatus(HttpServletRequest request) {
        try {
            Long hrUserId = getCurrentUserId(request);
            if (hrUserId == null) {
                return Result.error("未登录");
            }

            List<EnterpriseAudit> audits = enterpriseAuditService.findByHrUserId(hrUserId);
            if (audits == null || audits.isEmpty()) {
                Map<String, Object> data = new HashMap<>();
                data.put("auditStatus", -1); // 未提交
                data.put("message", "尚未提交认证申请");
                return Result.success(data);
            }

            // 返回最新的审核记录
            EnterpriseAudit latestAudit = audits.get(0);
            Map<String, Object> data = new HashMap<>();
            data.put("id", latestAudit.getId());
            data.put("auditStatus", latestAudit.getAuditStatus());
            data.put("businessLicenseImg", latestAudit.getBusinessLicenseImg());
            data.put("auditRemark", latestAudit.getAuditRemark());
            data.put("auditTime", latestAudit.getAuditTime());
            data.put("createTime", latestAudit.getCreateTime());

            String statusText;
            switch (latestAudit.getAuditStatus()) {
                case 0:
                    statusText = "待审核";
                    break;
                case 1:
                    statusText = "已通过";
                    break;
                case 2:
                    statusText = "已驳回";
                    break;
                default:
                    statusText = "未知";
            }
            data.put("statusText", statusText);

            log.info("查询企业认证状态成功: hrUserId={}, auditStatus={}", hrUserId, latestAudit.getAuditStatus());
            return Result.success(data);
        } catch (Exception e) {
            log.error("查询企业认证状态失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    // ==================== 岗位管理接口 ====================

    /**
     * 我企业的在招岗位列表
     */
    @GetMapping("/job/page")
    public Result jobPage(@RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer pageSize,
                          HttpServletRequest request) {
        try {
            Long hrUserId = getCurrentUserId(request);
            if (hrUserId == null) {
                return Result.error("未登录");
            }

            Long enterpriseId = getEnterpriseId(hrUserId);
            if (enterpriseId == null) {
                return Result.error("企业信息不存在");
            }

            List<RecruitJob> jobs = recruitJobService.findByEnterpriseIdPage(enterpriseId, page, pageSize);
            int total = recruitJobService.countByEnterpriseId(enterpriseId);

            Map<String, Object> data = new HashMap<>();
            data.put("list", jobs);
            data.put("total", total);
            data.put("page", page);
            data.put("pageSize", pageSize);
            data.put("totalPages", (total + pageSize - 1) / pageSize);

            log.info("查询企业岗位列表成功: enterpriseId={}, page={}, pageSize={}, total={}", 
                    enterpriseId, page, pageSize, total);
            return Result.success(data);
        } catch (Exception e) {
            log.error("查询企业岗位列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 岗位详情
     */
    @GetMapping("/job/{id}")
    public Result jobDetail(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long hrUserId = getCurrentUserId(request);
            if (hrUserId == null) {
                return Result.error("未登录");
            }

            RecruitJob job = recruitJobService.findById(id);
            if (job == null) {
                return Result.error("岗位不存在");
            }

            // 验证是否属于当前企业
            Long enterpriseId = getEnterpriseId(hrUserId);
            if (!job.getEnterpriseId().equals(enterpriseId)) {
                return Result.error("无权查看该岗位");
            }

            log.info("查看岗位详情: jobId={}, hrUserId={}", id, hrUserId);
            return Result.success(job);
        } catch (Exception e) {
            log.error("查询岗位详情失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 新增岗位（草稿状态）
     */
    @PostMapping("/job")
    public Result addJob(@RequestBody RecruitJob recruitJob, HttpServletRequest request) {
        try {
            Long hrUserId = getCurrentUserId(request);
            if (hrUserId == null) {
                return Result.error("未登录");
            }

            Long enterpriseId = getEnterpriseId(hrUserId);
            if (enterpriseId == null) {
                return Result.error("企业信息不存在");
            }

            recruitJob.setEnterpriseId(enterpriseId);
            recruitJob.setStatus(0);

            int result = recruitJobService.insert(recruitJob);
            if (result > 0) {
                log.info("新增岗位成功: enterpriseId={}, jobName={}", enterpriseId, recruitJob.getJobName());
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
            Long hrUserId = getCurrentUserId(request);
            if (hrUserId == null) {
                return Result.error("未登录");
            }

            if (recruitJob.getId() == null) {
                return Result.error("岗位ID不能为空");
            }

            Long enterpriseId = getEnterpriseId(hrUserId);
            if (enterpriseId == null) {
                return Result.error("企业信息不存在");
            }

            RecruitJob existingJob = recruitJobService.findById(recruitJob.getId());
            if (existingJob == null) {
                return Result.error("岗位不存在");
            }
            if (!existingJob.getEnterpriseId().equals(enterpriseId)) {
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
     * 删除岗位（逻辑删除）
     */
    @DeleteMapping("/job/{id}")
    public Result deleteJob(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long hrUserId = getCurrentUserId(request);
            if (hrUserId == null) {
                return Result.error("未登录");
            }

            Long enterpriseId = getEnterpriseId(hrUserId);
            if (enterpriseId == null) {
                return Result.error("企业信息不存在");
            }

            RecruitJob job = recruitJobService.findById(id);
            if (job == null) {
                return Result.error("岗位不存在");
            }
            if (!job.getEnterpriseId().equals(enterpriseId)) {
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

    /**
     * 发布岗位（从草稿变为已发布）
     */
    @PutMapping("/job/{id}/publish")
    public Result publishJob(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long hrUserId = getCurrentUserId(request);
            if (hrUserId == null) {
                return Result.error("未登录");
            }

            Long enterpriseId = getEnterpriseId(hrUserId);
            if (enterpriseId == null) {
                return Result.error("企业信息不存在");
            }

            RecruitJob job = recruitJobService.findById(id);
            if (job == null) {
                return Result.error("岗位不存在");
            }
            if (!job.getEnterpriseId().equals(enterpriseId)) {
                return Result.error("无权操作该岗位");
            }

            // 设置状态为已发布
            job.setStatus(1);
            int result = recruitJobService.update(job);
            if (result > 0) {
                log.info("发布岗位成功: jobId={}, enterpriseId={}", id, enterpriseId);
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
            Long hrUserId = getCurrentUserId(request);
            if (hrUserId == null) {
                return Result.error("未登录");
            }

            Long enterpriseId = getEnterpriseId(hrUserId);
            if (enterpriseId == null) {
                return Result.error("企业信息不存在");
            }

            RecruitJob job = recruitJobService.findById(id);
            if (job == null) {
                return Result.error("岗位不存在");
            }
            if (!job.getEnterpriseId().equals(enterpriseId)) {
                return Result.error("无权操作该岗位");
            }

            int result = recruitJobService.pauseRecruit(id);
            if (result > 0) {
                log.info("暂停岗位招聘: jobId={}", id);
                return Result.success();
            }
            return Result.error("操作失败");
        } catch (Exception e) {
            log.error("暂停岗位招聘失败", e);
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 关闭岗位
     */
    @PutMapping("/job/{id}/close")
    public Result closeJob(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long hrUserId = getCurrentUserId(request);
            if (hrUserId == null) {
                return Result.error("未登录");
            }

            Long enterpriseId = getEnterpriseId(hrUserId);
            if (enterpriseId == null) {
                return Result.error("企业信息不存在");
            }

            RecruitJob job = recruitJobService.findById(id);
            if (job == null) {
                return Result.error("岗位不存在");
            }
            if (!job.getEnterpriseId().equals(enterpriseId)) {
                return Result.error("无权操作该岗位");
            }

            int result = recruitJobService.closeRecruit(id);
            if (result > 0) {
                log.info("关闭岗位: jobId={}", id);
                return Result.success();
            }
            return Result.error("操作失败");
        } catch (Exception e) {
            log.error("关闭岗位失败", e);
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    // ==================== 简历收件箱接口 ====================

    /**
     * 简历收件箱列表（按岗位、状态筛选）
     */
    @GetMapping("/deliver/page")
    public Result deliverPage(@RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(required = false) Long jobId,
                              @RequestParam(required = false) Integer deliverStatus,
                              HttpServletRequest request) {
        try {
            Long hrUserId = getCurrentUserId(request);
            if (hrUserId == null) {
                return Result.error("未登录");
            }

            Long enterpriseId = getEnterpriseId(hrUserId);
            if (enterpriseId == null) {
                return Result.error("企业信息不存在");
            }

            List<com.example.pojo.dto.DeliverDetailDTO> delivers = jobDeliverService.findByEnterpriseIdPageWithDetail(
                    enterpriseId, jobId, deliverStatus, page, pageSize);
            int total = jobDeliverService.countByEnterpriseId(enterpriseId, jobId, deliverStatus);

            Map<String, Object> data = new HashMap<>();
            data.put("list", delivers);
            data.put("total", total);
            data.put("page", page);
            data.put("pageSize", pageSize);
            data.put("totalPages", (total + pageSize - 1) / pageSize);

            log.info("查询简历收件箱成功: enterpriseId={}, page={}, pageSize={}, total={}", 
                    enterpriseId, page, pageSize, total);
            return Result.success(data);
        } catch (Exception e) {
            log.error("查询简历收件箱失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 简历详情（手机号默认脱敏）
     */
    @GetMapping("/deliver/{id}")
    public Result deliverDetail(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long hrUserId = getCurrentUserId(request);
            if (hrUserId == null) {
                return Result.error("未登录");
            }

            JobDeliver deliver = jobDeliverService.findById(id);
            if (deliver == null) {
                return Result.error("投递记录不存在");
            }

            // 验证是否属于当前企业
            Long enterpriseId = getEnterpriseId(hrUserId);
            if (!deliver.getEnterpriseId().equals(enterpriseId)) {
                return Result.error("无权查看该投递记录");
            }

            // 获取学生简历
            StudentResume resume = studentResumeService.findById(deliver.getResumeId());
            if (resume == null) {
                return Result.error("简历不存在");
            }

            // 获取学生信息
            SysUser student = sysUserService.findById(deliver.getStudentUserId());
            if (student == null) {
                return Result.error("学生信息不存在");
            }

            // 构建返回数据，手机号脱敏
            Map<String, Object> data = new HashMap<>();
            data.put("deliver", deliver);
            data.put("resume", resume);
            
            Map<String, Object> studentInfo = new HashMap<>();
            studentInfo.put("id", student.getId());
            studentInfo.put("username", student.getUsername());
            studentInfo.put("realName", student.getRealName());
            studentInfo.put("email", student.getEmail());
            // 手机号脱敏：138****1234
            studentInfo.put("phone", maskPhone(student.getPhone()));
            
            data.put("studentInfo", studentInfo);

            log.info("查看简历详情: deliverId={}, hrUserId={}", id, hrUserId);
            return Result.success(data);
        } catch (Exception e) {
            log.error("查询简历详情失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 更新投递状态（待面试 / 不合适 / 已录用）
     */
    @PutMapping("/deliver/{id}/status")
    public Result updateDeliverStatus(@PathVariable Long id,
                                      @RequestBody Map<String, Object> requestData,
                                      HttpServletRequest request) {
        try {
            Long hrUserId = getCurrentUserId(request);
            if (hrUserId == null) {
                return Result.error("未登录");
            }

            JobDeliver deliver = jobDeliverService.findById(id);
            if (deliver == null) {
                return Result.error("投递记录不存在");
            }

            // 验证是否属于当前企业
            Long enterpriseId = getEnterpriseId(hrUserId);
            if (!deliver.getEnterpriseId().equals(enterpriseId)) {
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
                log.info("更新投递状态成功: deliverId={}, deliverStatus={}, hrUserId={}", id, deliverStatus, hrUserId);
                return Result.success();
            }
            return Result.error("更新状态失败");
        } catch (Exception e) {
            log.error("更新投递状态失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    // ==================== 面试通知接口 ====================

    /**
     * 面试通知列表（HR 查看自己发送的面试通知）
     */
    @GetMapping("/interview/page")
    public Result interviewPage(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                HttpServletRequest request) {
        try {
            Long hrUserId = getCurrentUserId(request);
            if (hrUserId == null) {
                return Result.error("未登录");
            }

            List<InterviewNotice> interviews = interviewNoticeService.findByHrUserIdPage(hrUserId, page, pageSize);
            int total = interviewNoticeService.countByHrUserId(hrUserId);

            Map<String, Object> data = new HashMap<>();
            data.put("list", interviews);
            data.put("total", total);
            data.put("page", page);
            data.put("pageSize", pageSize);
            data.put("totalPages", (total + pageSize - 1) / pageSize);

            log.info("查询面试通知列表成功: hrUserId={}, page={}, pageSize={}, total={}", hrUserId, page, pageSize, total);
            return Result.success(data);
        } catch (Exception e) {
            log.error("查询面试通知列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 给学生发送面试通知
     */
    @PostMapping("/interview/send")
    public Result sendInterview(@RequestBody InterviewNotice interviewNotice, HttpServletRequest request) {
        try {
            String token = request.getHeader("token");
            log.info("sendInterview - token: {}", token != null ? "exists" : "null");
            
            Long hrUserId = getCurrentUserId(request);
            log.info("sendInterview - hrUserId: {}", hrUserId);
            
            if (hrUserId == null) {
                return Result.error("未登录");
            }

            // 验证投递记录是否属于当前企业
            JobDeliver deliver = jobDeliverService.findById(interviewNotice.getDeliverId());
            if (deliver == null) {
                return Result.error("投递记录不存在");
            }

            Long enterpriseId = getEnterpriseId(hrUserId);
            if (!deliver.getEnterpriseId().equals(enterpriseId)) {
                return Result.error("无权操作该投递记录");
            }

            // 设置 HR 用户 ID
            interviewNotice.setHrUserId(hrUserId);
            interviewNotice.setStudentUserId(deliver.getStudentUserId());
            interviewNotice.setJobId(deliver.getJobId());

            // 发送面试通知
            int result = interviewNoticeService.sendInterviewNotice(interviewNotice);
            if (result > 0) {
                // 更新投递状态为待面试（直接调用mapper，避免markAsInterview内部再次创建面试通知）
                jobDeliverMapper.updateDeliverStatus(deliver.getId(), 1, "已发送面试通知");
                
                log.info("发送面试通知成功: deliverId={}, studentUserId={}, hrUserId={}", 
                        interviewNotice.getDeliverId(), interviewNotice.getStudentUserId(), hrUserId);
                return Result.success();
            }
            return Result.error("发送面试通知失败");
        } catch (Exception e) {
            log.error("发送面试通知失败", e);
            return Result.error("发送失败: " + e.getMessage());
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

    /**
     * 获取 HR 用户绑定的企业 ID
     */
    private Long getEnterpriseId(Long hrUserId) {
        SysUser hrUser = sysUserService.findById(hrUserId);
        if (hrUser == null || hrUser.getEnterpriseId() == null) {
            return null;
        }
        return hrUser.getEnterpriseId().longValue();
    }

    /**
     * 手机号脱敏：138****1234
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
