package com.example.controller;

import com.example.pojo.Result;
import com.example.pojo.SysNotice;
import com.example.service.SysNoticeService;
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
 * 站内消息接口控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    @Autowired
    private SysNoticeService sysNoticeService;

    /**
     * 我的消息列表（分页）
     */
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) Integer isRead,
                       HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                return Result.error("未登录");
            }

            List<SysNotice> notices = sysNoticeService.findByReceiveUserIdPage(userId, isRead, page, pageSize);
            int total = sysNoticeService.countByReceiveUserId(userId, isRead);

            Map<String, Object> data = new HashMap<>();
            data.put("list", notices);
            data.put("total", total);
            data.put("page", page);
            data.put("pageSize", pageSize);
            data.put("totalPages", (total + pageSize - 1) / pageSize);

            log.info("查询消息列表成功: userId={}, page={}, pageSize={}, total={}", userId, page, pageSize, total);
            return Result.success(data);
        } catch (Exception e) {
            log.error("查询消息列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 全部标记为已读
     */
    @PutMapping("/all/read")
    public Result markAllAsRead(HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                return Result.error("未登录");
            }

            int result = sysNoticeService.markAllAsRead(userId);
            log.info("全部标记为已读成功: userId={}, count={}", userId, result);
            return Result.success();
        } catch (Exception e) {
            log.error("全部标记为已读失败", e);
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 单个消息标记为已读
     */
    @PutMapping("/{id}/read")
    public Result markAsRead(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                return Result.error("未登录");
            }

            SysNotice notice = sysNoticeService.findById(id);
            if (notice == null) {
                return Result.error("消息不存在");
            }

            if (!userId.equals(notice.getReceiveUserId())) {
                return Result.error("无权操作该消息");
            }

            int result = sysNoticeService.markAsRead(id);
            log.info("标记消息为已读成功: userId={}, noticeId={}", userId, id);
            return Result.success();
        } catch (Exception e) {
            log.error("标记消息为已读失败", e);
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
