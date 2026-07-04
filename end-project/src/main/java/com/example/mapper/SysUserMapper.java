package com.example.mapper;

import com.example.pojo.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 系统用户表Mapper接口
 */
@Mapper
public interface SysUserMapper {
    
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
     * 分页查询用户列表（支持按角色、专业、关键词筛选）
     */
    List<SysUser> findByPage(@Param("userType") String userType,
                             @Param("majorId") Integer majorId,
                             @Param("keyword") String keyword,
                             @Param("offset") int offset,
                             @Param("pageSize") int pageSize);

    /**
     * 统计用户数量（支持按角色、专业、关键词筛选）
     */
    int countByCondition(@Param("userType") String userType,
                         @Param("majorId") Integer majorId,
                         @Param("keyword") String keyword);

    @Select("select id, username, password, real_name, user_type, phone, email, avatar, major_id, enterprise_id, status, create_time, update_time, is_deleted from sys_user where username=#{username} and password = #{password} and is_deleted = 0")
    SysUser selectByUsernameAndPassword(SysUser sysUser);

    /**
     * 根据绑定企业ID查询HR用户
     */
    List<SysUser> findByEnterpriseId(Integer enterpriseId);

    /**
     * 查询分专业就业率统计
     */
    List<Map<String, Object>> findMajorEmploymentStats();

    /**
     * 根据状态查询用户列表（分页）
     */
    List<SysUser> findByStatus(Integer status, int offset, int pageSize);

    /**
     * 根据状态统计用户数量
     */
    int countByStatus(Integer status);
}
