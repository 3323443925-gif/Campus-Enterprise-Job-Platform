package com.example.service.impl;

import com.example.mapper.SysUserMapper;
import com.example.pojo.LoginInfo;
import com.example.pojo.SysUser;
import com.example.service.SysUserService;
import com.example.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统用户服务实现类
 */
@Slf4j
@Service
public class SysUserServiceImpl implements SysUserService {
    
    @Autowired
    private SysUserMapper sysUserMapper;
    
    @Override
    public List<SysUser> findAll() {
        return sysUserMapper.findAll();
    }
    
    @Override
    public SysUser findById(Long id) {
        return sysUserMapper.findById(id);
    }
    
    @Override
    public SysUser findByUsername(String username) {
        return sysUserMapper.findByUsername(username);
    }
    
    @Override
    public List<SysUser> findByUserType(String userType) {
        return sysUserMapper.findByUserType(userType);
    }
    
    @Override
    public List<SysUser> findByMajorId(Long majorId) {
        return sysUserMapper.findByMajorId(majorId);
    }
    
    @Override
    public int insert(SysUser sysUser) {
        // 密码加密
        if (sysUser.getPassword() != null) {
            sysUser.setPassword(encryptPassword(sysUser.getPassword()));
        }
        // 默认状态为正常
        if (sysUser.getStatus() == null) {
            sysUser.setStatus(1);
        }
        return sysUserMapper.insert(sysUser);
    }
    
    @Override
    public int update(SysUser sysUser) {
        // 如果更新了密码，需要加密
        if (sysUser.getPassword() != null && !sysUser.getPassword().isEmpty()) {
            sysUser.setPassword(encryptPassword(sysUser.getPassword()));
        }
        return sysUserMapper.update(sysUser);
    }
    
    @Override
    public int updateProfile(SysUser sysUser) {
        return sysUserMapper.updateProfile(sysUser);
    }
    
    @Override
    public int deleteById(Long id) {
        return sysUserMapper.deleteById(id);
    }
    
    @Override
    public List<SysUser> findByPage(String userType, Integer majorId, String keyword, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return sysUserMapper.findByPage(userType, majorId, keyword, offset, pageSize);
    }
    
    @Override
    public int countByCondition(String userType, Integer majorId, String keyword) {
        return sysUserMapper.countByCondition(userType, majorId, keyword);
    }
    
    @Override
    public LoginInfo login(SysUser sysUser) {
        String encryptedPassword = encryptPassword(sysUser.getPassword());
        sysUser.setPassword(encryptedPassword);
        
        SysUser user = sysUserMapper.selectByUsernameAndPassword(sysUser);

        if (user == null){
            return null;
        }

        // 验证用户状态：0-禁用/待审核，1-正常
        if (user.getStatus() == null || user.getStatus() != 1) {
            log.warn("用户登录失败，状态异常: userId={}, username={}, status={}", 
                    user.getId(), user.getUsername(), user.getStatus());
            return null;
        }

        Map<String,Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("username", user.getUsername());
        String jwt = JwtUtils.generateToken(claims);

        log.info("登陆成功{}", user);
        return new LoginInfo(user.getId(), user.getUsername(), user.getPassword(), user.getUserType(), jwt);
    }
    
    @Override
    public int changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = sysUserMapper.findById(userId);
        if (user == null) {
            return 0;
        }
        
        // 验证旧密码
        String encryptedOldPassword = encryptPassword(oldPassword);
        if (!encryptedOldPassword.equals(user.getPassword())) {
            return 0;
        }
        
        // 更新新密码
        SysUser updateUser = new SysUser();
        updateUser.setId(userId);
        updateUser.setPassword(newPassword);
        return sysUserMapper.update(updateUser);
    }

    @Override
    public List<Map<String, Object>> findMajorEmploymentStats() {
        return sysUserMapper.findMajorEmploymentStats();
    }

    @Override
    public List<SysUser> findByStatus(Integer status, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return sysUserMapper.findByStatus(status, offset, pageSize);
    }

    @Override
    public int countByStatus(Integer status) {
        return sysUserMapper.countByStatus(status);
    }
    
    /**
     * 密码加密（MD5）
     */
    private String encryptPassword(String password) {
        byte[] digest = DigestUtils.md5Digest(password.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(digest);
    }
    
    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
