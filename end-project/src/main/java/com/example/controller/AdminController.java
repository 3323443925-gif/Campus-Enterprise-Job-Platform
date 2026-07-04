package com.example.controller;

import com.example.pojo.EnterpriseAudit;
import com.example.pojo.Major;
import com.example.pojo.Result;
import com.example.pojo.SysUser;
import com.example.service.EnterpriseAuditService;
import com.example.service.MajorService;
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
 * 管理员/就业办端控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private MajorService majorService;

    @Autowired
    private EnterpriseAuditService enterpriseAuditService;

    // ==================== 用户管理接口 ====================

    /**
     * 用户分页列表（支持按角色、专业、关键词筛选）
     */
    @GetMapping("/user/page")
    public Result userPage(@RequestParam(defaultValue = "1") Integer page,
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           @RequestParam(required = false) String userType,
                           @RequestParam(required = false) Integer majorId,
                           @RequestParam(required = false) String keyword) {
        try {
            List<SysUser> users = sysUserService.findByPage(userType, majorId, keyword, page, pageSize);
            int total = sysUserService.countByCondition(userType, majorId, keyword);

            Map<String, Object> data = new HashMap<>();
            data.put("list", users);
            data.put("total", total);
            data.put("page", page);
            data.put("pageSize", pageSize);
            data.put("totalPages", (total + pageSize - 1) / pageSize);

            log.info("查询用户分页列表成功: page={}, pageSize={}, total={}", page, pageSize, total);
            return Result.success(data);
        } catch (Exception e) {
            log.error("查询用户分页列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 新增用户（教师、学生、HR 账号）
     */
    @PostMapping("/user")
    public Result addUser(@RequestBody SysUser sysUser) {
        try {
            // 检查用户名是否已存在
            SysUser existingUser = sysUserService.findByUsername(sysUser.getUsername());
            if (existingUser != null) {
                return Result.error("用户名已存在");
            }

            int result = sysUserService.insert(sysUser);
            if (result > 0) {
                log.info("新增用户成功: username={}, userType={}", sysUser.getUsername(), sysUser.getUserType());
                return Result.success();
            }
            return Result.error("新增用户失败");
        } catch (Exception e) {
            log.error("新增用户失败", e);
            return Result.error("新增用户失败: " + e.getMessage());
        }
    }

    /**
     * 修改用户基础信息、账号状态
     */
    @PutMapping("/user")
    public Result updateUser(@RequestBody SysUser sysUser) {
        try {
            if (sysUser.getId() == null) {
                return Result.error("用户ID不能为空");
            }

            int result = sysUserService.update(sysUser);
            if (result > 0) {
                log.info("更新用户成功: userId={}", sysUser.getId());
                return Result.success();
            }
            return Result.error("更新用户失败");
        } catch (Exception e) {
            log.error("更新用户失败", e);
            return Result.error("更新用户失败: " + e.getMessage());
        }
    }

    /**
     * 逻辑删除用户
     */
    @DeleteMapping("/user/{id}")
    public Result deleteUser(@PathVariable Long id) {
        try {
            int result = sysUserService.deleteById(id);
            if (result > 0) {
                log.info("删除用户成功: userId={}", id);
                return Result.success();
            }
            return Result.error("删除用户失败");
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return Result.error("删除用户失败: " + e.getMessage());
        }
    }

    // ==================== 用户审核接口 ====================

    /**
     * 待审核用户列表（HR和教师）
     */
    @GetMapping("/user/audit/page")
    public Result userAuditPage(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            List<SysUser> users = sysUserService.findByStatus(0, page, pageSize);
            int total = sysUserService.countByStatus(0);

            Map<String, Object> data = new HashMap<>();
            data.put("list", users);
            data.put("total", total);
            data.put("page", page);
            data.put("pageSize", pageSize);
            data.put("totalPages", (total + pageSize - 1) / pageSize);

            log.info("查询待审核用户列表成功: page={}, pageSize={}, total={}", page, pageSize, total);
            return Result.success(data);
        } catch (Exception e) {
            log.error("查询待审核用户列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 用户审核通过
     */
    @PutMapping("/user/audit/{id}/pass")
    public Result userAuditPass(@PathVariable Long id) {
        try {
            SysUser user = sysUserService.findById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }

            if (user.getStatus() != 0) {
                return Result.error("该用户无需审核");
            }

            SysUser updateUser = new SysUser();
            updateUser.setId(id);
            updateUser.setStatus(1);
            
            int result = sysUserService.update(updateUser);
            if (result > 0) {
                log.info("用户审核通过: userId={}, username={}", id, user.getUsername());
                return Result.success();
            }
            return Result.error("审核操作失败");
        } catch (Exception e) {
            log.error("用户审核通过失败", e);
            return Result.error("审核失败: " + e.getMessage());
        }
    }

    /**
     * 用户审核驳回（删除用户）
     */
    @DeleteMapping("/user/audit/{id}/reject")
    public Result userAuditReject(@PathVariable Long id) {
        try {
            SysUser user = sysUserService.findById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }

            if (user.getStatus() != 0) {
                return Result.error("该用户无需审核");
            }

            int result = sysUserService.deleteById(id);
            if (result > 0) {
                log.info("用户审核驳回: userId={}, username={}", id, user.getUsername());
                return Result.success();
            }
            return Result.error("驳回操作失败");
        } catch (Exception e) {
            log.error("用户审核驳回失败", e);
            return Result.error("驳回失败: " + e.getMessage());
        }
    }

    // ==================== 专业管理接口 ====================

    /**
     * 专业全量列表
     */
    @GetMapping("/major/list")
    public Result majorList() {
        try {
            List<Major> majors = majorService.findAll();
            log.info("查询专业列表成功: count={}", majors.size());
            return Result.success(majors);
        } catch (Exception e) {
            log.error("查询专业列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 新增专业
     */
    @PostMapping("/major")
    public Result addMajor(@RequestBody Major major) {
        try {
            // 检查专业编码是否已存在
            if (major.getMajorCode() != null) {
                Major existingMajor = majorService.findByMajorCode(major.getMajorCode());
                if (existingMajor != null) {
                    return Result.error("专业编码已存在");
                }
            }

            int result = majorService.insert(major);
            if (result > 0) {
                log.info("新增专业成功: majorName={}", major.getMajorName());
                return Result.success();
            }
            return Result.error("新增专业失败");
        } catch (Exception e) {
            log.error("新增专业失败", e);
            return Result.error("新增专业失败: " + e.getMessage());
        }
    }

    /**
     * 修改专业信息
     */
    @PutMapping("/major")
    public Result updateMajor(@RequestBody Major major) {
        try {
            if (major.getId() == null) {
                return Result.error("专业ID不能为空");
            }

            int result = majorService.update(major);
            if (result > 0) {
                log.info("更新专业成功: majorId={}", major.getId());
                return Result.success();
            }
            return Result.error("更新专业失败");
        } catch (Exception e) {
            log.error("更新专业失败", e);
            return Result.error("更新专业失败: " + e.getMessage());
        }
    }

    // ==================== 企业资质审核接口 ====================

    /**
     * 企业资质审核列表（待审核 / 已通过 / 驳回筛选）
     */
    @GetMapping("/audit/page")
    public Result auditPage(@RequestParam(defaultValue = "1") Integer page,
                            @RequestParam(defaultValue = "10") Integer pageSize,
                            @RequestParam(required = false) Integer auditStatus,
                            HttpServletRequest request) {
        try {
            List<EnterpriseAudit> audits = enterpriseAuditService.findByPage(auditStatus, page, pageSize);
            int total = enterpriseAuditService.countByStatus(auditStatus);

            Map<String, Object> data = new HashMap<>();
            data.put("list", audits);
            data.put("total", total);
            data.put("page", page);
            data.put("pageSize", pageSize);
            data.put("totalPages", (total + pageSize - 1) / pageSize);

            log.info("查询审核列表成功: page={}, pageSize={}, total={}", page, pageSize, total);
            return Result.success(data);
        } catch (Exception e) {
            log.error("查询审核列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 企业认证审核通过
     */
    @PutMapping("/audit/{id}/pass")
    public Result auditPass(@PathVariable Long id,
                            @RequestParam(required = false) String auditRemark,
                            HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            if (currentUserId == null) {
                return Result.error("未登录");
            }

            int result = enterpriseAuditService.approve(id, currentUserId, auditRemark);
            if (result > 0) {
                log.info("审核通过: auditId={}, auditUserId={}", id, currentUserId);
                return Result.success();
            }
            return Result.error("审核操作失败");
        } catch (Exception e) {
            log.error("审核通过失败", e);
            return Result.error("审核失败: " + e.getMessage());
        }
    }

    /**
     * 企业认证审核驳回
     */
    @PutMapping("/audit/{id}/reject")
    public Result auditReject(@PathVariable Long id,
                              @RequestParam(required = false) String auditRemark,
                              HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            if (currentUserId == null) {
                return Result.error("未登录");
            }

            int result = enterpriseAuditService.reject(id, currentUserId, auditRemark);
            if (result > 0) {
                log.info("审核驳回: auditId={}, auditUserId={}", id, currentUserId);
                return Result.success();
            }
            return Result.error("审核操作失败");
        } catch (Exception e) {
            log.error("审核驳回失败", e);
            return Result.error("审核失败: " + e.getMessage());
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
