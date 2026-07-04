package com.example.controller;

import com.example.pojo.LoginInfo;
import com.example.pojo.Result;
import com.example.pojo.SysUser;
import com.example.service.SysUserService;
import com.example.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 统一登录接口
     */
    @PostMapping("/login")
    public Result login(@RequestBody SysUser user){
        log.info("登录认证{}", user);
        LoginInfo info = sysUserService.login(user);
        if (info != null){
            // 构建响应数据，包含用户信息和跳转路径
            Map<String, Object> data = new HashMap<>();
            data.put("userInfo", info);
            data.put("userType", info.getUserType());
            data.put("homePath", getHomePathByUserType(info.getUserType()));

            log.info("用户登录成功: userId={}, userType={}, homePath={}",
                    info.getId(), info.getUserType(), data.get("homePath"));

            return Result.success(data);
        }
        
        // 检查用户是否存在但状态异常
        SysUser existUser = sysUserService.findByUsername(user.getUsername());
        if (existUser != null && existUser.getStatus() != 1) {
            log.warn("用户登录失败，状态为待审核或禁用: username={}, status={}", 
                    user.getUsername(), existUser.getStatus());
            return Result.error("您的账号正在审核中，请等待管理员审核通过后再登录");
        }
        
        return Result.error("用户名或密码错误");
    }

    /**
     * 获取当前登录用户详情、角色、权限
     */
    @GetMapping("/info")
    public Result info(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            return Result.error("未登录");
        }

        try {
            Claims claims = JwtUtils.parseToken(token);
            Long userId = Long.valueOf(claims.get("id").toString());
            String username = claims.get("username").toString();

            SysUser user = sysUserService.findById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 构建用户详情响应
            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("realName", user.getRealName());
            data.put("userType", user.getUserType());
            data.put("phone", user.getPhone());
            data.put("email", user.getEmail());
            data.put("avatar", user.getAvatar());
            data.put("majorId", user.getMajorId());
            data.put("enterpriseId", user.getEnterpriseId());

            // 根据用户类型返回对应的权限菜单
            data.put("roles", getRolesByUserType(user.getUserType()));
            data.put("permissions", getPermissionsByUserType(user.getUserType()));

            log.info("获取用户信息成功: userId={}, userType={}", userId, user.getUserType());
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.error("获取用户信息失败");
        }
    }

    /**
     * 退出登录，销毁 token
     */
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token != null && !token.isEmpty()) {
            try {
                Claims claims = JwtUtils.parseToken(token);
                Long userId = Long.valueOf(claims.get("id").toString());
                log.info("用户退出登录: userId={}", userId);
            } catch (Exception e) {
                log.error("解析退出登录的 token 失败", e);
            }
        }
        // JWT 是无状态的，服务端不需要额外操作，前端清除 token 即可
        return Result.success();
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/profile")
    public Result getProfile(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            return Result.error("未登录");
        }
        
        try {
            Claims claims = JwtUtils.parseToken(token);
            Long userId = Long.valueOf(claims.get("id").toString());
            SysUser user = sysUserService.findById(userId);
            
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("username", user.getUsername());
            profile.put("realName", user.getRealName());
            profile.put("userType", user.getUserType());
            profile.put("phone", user.getPhone());
            profile.put("email", user.getEmail());
            profile.put("avatar", user.getAvatar());
            profile.put("majorId", user.getMajorId());
            profile.put("createTime", user.getCreateTime());
            profile.put("updateTime", user.getUpdateTime());
            
            return Result.success(profile);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.error("获取用户信息失败");
        }
    }

    /**
     * 更新当前登录用户个人资料
     */
    @PutMapping("/profile")
    public Result updateProfile(@RequestBody SysUser user, HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            return Result.error("未登录");
        }
        
        try {
            Claims claims = JwtUtils.parseToken(token);
            Long userId = Long.valueOf(claims.get("id").toString());
            
            user.setId(userId);
            
            int result = sysUserService.updateProfile(user);
            if (result > 0) {
                log.info("用户更新个人资料成功: userId={}", userId);
                return Result.success();
            }
            return Result.error("更新失败");
        } catch (Exception e) {
            log.error("更新用户资料失败", e);
            return Result.error("更新用户资料失败: " + e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result changePassword(@RequestBody Map<String, String> requestData, HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            return Result.error("未登录");
        }
        
        String oldPassword = requestData.get("oldPassword");
        String newPassword = requestData.get("newPassword");
        
        if (oldPassword == null || oldPassword.isEmpty()) {
            return Result.error("旧密码不能为空");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return Result.error("新密码不能为空");
        }
        
        try {
            Claims claims = JwtUtils.parseToken(token);
            Long userId = Long.valueOf(claims.get("id").toString());
            
            int result = sysUserService.changePassword(userId, oldPassword, newPassword);
            if (result > 0) {
                log.info("用户修改密码成功: userId={}", userId);
                return Result.success();
            }
            return Result.error("旧密码错误");
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return Result.error("修改密码失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户类型获取首页路径
     */
    private String getHomePathByUserType(String userType) {
        if (userType == null) {
            return "/";
        }

        switch (userType) {
            case "admin":
                return "/admin/dashboard";           // 系统管理员后台
            case "job_office":
                return "/teacher/enterprise";        // 就业办教师 - 企业管理
            case "teacher":
                return "/teacher/index";              // 专业班主任 - 首页
            case "student":
                return "/student/jobs";              // 学生 - 岗位浏览
            case "hr":
                return "/hr/recruit";                // 企业HR - 招聘管理
            default:
                return "/";
        }
    }

    /**
     * 根据用户类型获取角色列表
     */
    private String[] getRolesByUserType(String userType) {
        return new String[]{userType};
    }

    /**
     * 根据用户类型获取权限菜单
     */
    private String[] getPermissionsByUserType(String userType) {
        if (userType == null) {
            return new String[]{};
        }

        switch (userType) {
            case "admin":
                return new String[]{"sys:user:manage", "sys:role:manage", "sys:notice:manage", "enterprise:audit", "recruit:manage", "student:manage"};
            case "job_office":
                return new String[]{"enterprise:audit", "enterprise:manage", "notice:manage", "recruit:view"};
            case "teacher":
                return new String[]{"student:manage", "student:resume:view", "recruit:view"};
            case "student":
                return new String[]{"recruit:view", "resume:manage", "interview:view"};
            case "hr":
                return new String[]{"recruit:manage", "job:manage", "student:resume:view", "interview:manage"};
            default:
                return new String[]{};
        }
    }
}
