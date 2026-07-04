package com.example.mapper;

import com.example.pojo.SysNotice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 站内消息表Mapper接口
 */
@Mapper
public interface SysNoticeMapper {
    
    /**
     * 查询所有消息
     */
    List<SysNotice> findAll();
    
    /**
     * 根据ID查询消息
     */
    SysNotice findById(Long id);
    
    /**
     * 根据接收人ID查询消息
     */
    List<SysNotice> findByReceiveUserId(Long receiveUserId);
    
    /**
     * 根据接收人ID查询未读消息
     */
    List<SysNotice> findUnreadByReceiveUserId(Long receiveUserId);
    
    /**
     * 根据消息类型查询
     */
    List<SysNotice> findByNoticeType(Integer noticeType);
    
    /**
     * 新增消息
     */
    int insert(SysNotice sysNotice);
    
    /**
     * 更新消息
     */
    int update(SysNotice sysNotice);
    
    /**
     * 标记消息为已读
     */
    int markAsRead(Long id);
    
    /**
     * 删除消息
     */
    int deleteById(Long id);

    /**
     * 分页查询接收人的消息
     */
    List<SysNotice> findByReceiveUserIdPage(@Param("receiveUserId") Long receiveUserId,
                                            @Param("isRead") Integer isRead,
                                            @Param("offset") int offset,
                                            @Param("pageSize") int pageSize);

    /**
     * 统计接收人的消息数量
     */
    int countByReceiveUserId(@Param("receiveUserId") Long receiveUserId,
                             @Param("isRead") Integer isRead);
}
