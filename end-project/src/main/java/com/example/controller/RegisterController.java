package com.example.controller;

import com.example.mapper.MajorMapper;
import com.example.mapper.EnterpriseMapper;
import com.example.pojo.Enterprise;
import com.example.pojo.Major;
import com.example.pojo.Result;
import com.example.pojo.SysUser;
import com.example.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户注册控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class RegisterController {
    
    @Autowired
    private SysUserService sysUserService;
    
    @Autowired
    private MajorMapper majorMapper;
    
    @Autowired
    private EnterpriseMapper enterpriseMapper;
    
    /**
     * 学生注册
     */
    @PostMapping("/register/student")
    public Result registerStudent(@RequestBody RegisterRequest request) {
        log.info("学生注册请求: {}", request);
        
        // 验证必填字段
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            return Result.error("学号不能为空");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return Result.error("密码不能为空");
        }
        if (request.getRealName() == null || request.getRealName().isEmpty()) {
            return Result.error("姓名不能为空");
        }
        
        // 检查用户名是否已存在
        SysUser existUser = sysUserService.findByUsername(request.getUsername());
        if (existUser != null) {
            return Result.error("该学号已被注册");
        }
        
        // 创建学生用户
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());  // 学号
        user.setPassword(request.getPassword());
        user.setRealName(request.getRealName());
        user.setUserType("student");
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setMajorId(request.getMajorId());
        user.setStatus(1);  // 默认启用
        
        int result = sysUserService.insert(user);
        
        if (result > 0) {
            log.info("学生注册成功: username={}", request.getUsername());
            return Result.success("注册成功");
        }
        
        return Result.error("注册失败，请稍后重试");
    }
    
    /**
     * 企业HR注册
     */
    @PostMapping("/register/hr")
    public Result registerHr(@RequestBody RegisterRequest request) {
        log.info("企业HR注册请求: {}", request);
        
        // 验证必填字段
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            return Result.error("手机号不能为空");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return Result.error("密码不能为空");
        }
        if (request.getRealName() == null || request.getRealName().isEmpty()) {
            return Result.error("姓名不能为空");
        }
        
        // 检查用户名是否已存在
        SysUser existUser = sysUserService.findByUsername(request.getUsername());
        if (existUser != null) {
            return Result.error("该手机号已被注册");
        }
        
        // 创建HR用户
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());  // 手机号
        user.setPassword(request.getPassword());
        user.setRealName(request.getRealName());
        user.setUserType("hr");
        user.setPhone(request.getUsername());  // 手机号同时作为联系方式
        user.setEmail(request.getEmail());
        user.setEnterpriseId(request.getEnterpriseId());  // 企业ID可为空，认证后再关联
        user.setStatus(0);  // 默认禁用，需要审核
        
        int result = sysUserService.insert(user);
        
        if (result > 0) {
            log.info("企业HR注册成功: username={}, 等待审核", request.getUsername());
            return Result.success("注册成功，请等待管理员审核");
        }
        
        return Result.error("注册失败，请稍后重试");
    }
    
    /**
     * 教师注册（通常由管理员创建，这里预留接口）
     */
    @PostMapping("/register/teacher")
    public Result registerTeacher(@RequestBody RegisterRequest request) {
        log.info("教师注册请求: {}", request);
        
        // 验证必填字段
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            return Result.error("工号不能为空");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return Result.error("密码不能为空");
        }
        if (request.getRealName() == null || request.getRealName().isEmpty()) {
            return Result.error("姓名不能为空");
        }
        
        // 检查用户名是否已存在
        SysUser existUser = sysUserService.findByUsername(request.getUsername());
        if (existUser != null) {
            return Result.error("该工号已被注册");
        }
        
        // 创建教师用户
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());  // 工号
        user.setPassword(request.getPassword());
        user.setRealName(request.getRealName());
        user.setUserType(request.getUserType() != null ? request.getUserType() : "teacher");
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setMajorId(request.getMajorId());
        user.setStatus(0);  // 默认禁用，需要管理员激活
        
        int result = sysUserService.insert(user);
        
        if (result > 0) {
            log.info("教师注册成功: username={}, 等待激活", request.getUsername());
            return Result.success("注册成功，请等待管理员激活");
        }
        
        return Result.error("注册失败，请稍后重试");
    }
    
    /**
     * 检查用户名是否可用
     */
    @GetMapping("/check-username")
    public Result checkUsername(@RequestParam String username) {
        if (username == null || username.isEmpty()) {
            return Result.error("用户名不能为空");
        }
        
        SysUser user = sysUserService.findByUsername(username);
        if (user != null) {
            return Result.success("该用户名已被使用");
        }
        
        return Result.success("该用户名可用");
    }
    
    /**
     * 获取专业列表（公开接口，供注册使用）
     */
    @GetMapping("/majors")
    public Result getMajorList() {
        try {
            List<Major> majors = majorMapper.findAll();
            return Result.success(majors);
        } catch (Exception e) {
            log.error("获取专业列表失败", e);
            return Result.error("获取专业列表失败");
        }
    }
    
    /**
     * 获取企业列表（公开接口，供注册使用）
     */
    @GetMapping("/enterprises")
    public Result getEnterpriseList() {
        try {
            List<Enterprise> enterprises = enterpriseMapper.findAll();
            return Result.success(enterprises);
        } catch (Exception e) {
            log.error("获取企业列表失败", e);
            return Result.error("获取企业列表失败");
        }
    }
    
    /**
     * 注册请求DTO
     */
    public static class RegisterRequest {
        private String username;      // 用户名（学号/手机号/工号）
        private String password;      // 密码
        private String realName;      // 真实姓名
        private String userType;      // 用户类型（teacher/job_office）
        private String phone;         // 手机号
        private String email;         // 邮箱
        private Integer majorId;      // 专业ID（学生/教师）
        private Integer enterpriseId; // 企业ID（HR）
        
        // Getter and Setter methods
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public String getRealName() {
            return realName;
        }
        
        public void setRealName(String realName) {
            this.realName = realName;
        }
        
        public String getUserType() {
            return userType;
        }
        
        public void setUserType(String userType) {
            this.userType = userType;
        }
        
        public String getPhone() {
            return phone;
        }
        
        public void setPhone(String phone) {
            this.phone = phone;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public Integer getMajorId() {
            return majorId;
        }
        
        public void setMajorId(Integer majorId) {
            this.majorId = majorId;
        }
        
        public Integer getEnterpriseId() {
            return enterpriseId;
        }
        
        public void setEnterpriseId(Integer enterpriseId) {
            this.enterpriseId = enterpriseId;
        }
        
        @Override
        public String toString() {
            return "RegisterRequest{" +
                    "username='" + username + '\'' +
                    ", realName='" + realName + '\'' +
                    ", userType='" + userType + '\'' +
                    ", phone='" + phone + '\'' +
                    ", email='" + email + '\'' +
                    ", majorId=" + majorId +
                    ", enterpriseId=" + enterpriseId +
                    '}';
        }
    }
}
