package com.example.service;

import com.example.pojo.LoginInfo;
import com.example.pojo.SysUser;
import java.util.List;
import java.util.Map;

/**
 * 系统用户服务接口
 */
public interface SysUserService {
    
    /**
     * 查询所有用户
     */
    List<SysUser> findAll();
    
    /**
     * 根据ID查询用户
     */
    SysUser findById(Long id);
    
    /**
     * 根据用户名查询用户
     */
    SysUser findByUsername(String username);
    
    /**
     * 根据用户类型查询用户列表
     */
    List<SysUser> findByUserType(String userType);
    
    /**
     * 根据专业ID查询用户
     */
    List<SysUser> findByMajorId(Long majorId);
    
    /**
     * 新增用户
     */
    int insert(SysUser sysUser);
    
    /**
     * 更新用户
     */
    int update(SysUser sysUser);
    
    /**
     * 更新用户个人资料（不含密码）
     */
    int updateProfile(SysUser sysUser);
    
    /**
     * 删除用户（逻辑删除）
     */
    int deleteById(Long id);
    
    /**
     * 分页查询用户列表
     */
    List<SysUser> findByPage(String userType, Integer majorId, String keyword, int page, int pageSize);

    /**
     * 统计用户数量
     */
    int countByCondition(String userType, Integer majorId, String keyword);
    
    /**
     * 用户登录
     */
    LoginInfo login(SysUser sysUser);

    /**
     * 修改密码
     */
    int changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 查询分专业就业率统计
     */
    List<Map<String, Object>> findMajorEmploymentStats();

    /**
     * 根据状态查询用户列表（分页）
     */
    List<SysUser> findByStatus(Integer status, int page, int pageSize);

    /**
     * 根据状态统计用户数量
     */
    int countByStatus(Integer status);
}
