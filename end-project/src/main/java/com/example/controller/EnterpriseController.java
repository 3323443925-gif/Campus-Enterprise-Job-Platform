package com.example.controller;

import com.example.pojo.Enterprise;
import com.example.pojo.RecruitJob;
import com.example.pojo.Result;
import com.example.service.EnterpriseService;
import com.example.service.RecruitJobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 校企资源库接口控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/enterprise")
public class EnterpriseController {

    @Autowired
    private EnterpriseService enterpriseService;

    @Autowired
    private RecruitJobService recruitJobService;

    /**
     * 企业列表（支持关键词、行业筛选）
     */
    @GetMapping("/list")
    public Result list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String industry) {
        try {
            List<Enterprise> enterprises = enterpriseService.findByCondition(keyword, industry);
            log.info("查询企业列表成功: keyword={}, industry={}, count={}", keyword, industry, enterprises.size());
            return Result.success(enterprises);
        } catch (Exception e) {
            log.error("查询企业列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 企业详情（基础信息 + 历史岗位）
     */
    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) {
        try {
            Enterprise enterprise = enterpriseService.findById(id);
            if (enterprise == null) {
                return Result.error("企业不存在");
            }

            // 获取企业的历史岗位
            List<RecruitJob> jobs = recruitJobService.findByEnterpriseId(id);

            Map<String, Object> data = new HashMap<>();
            data.put("enterprise", enterprise);
            data.put("jobs", jobs);

            log.info("查询企业详情成功: enterpriseId={}, companyName={}, jobCount={}", 
                    id, enterprise.getCompanyName(), jobs.size());
            return Result.success(data);
        } catch (Exception e) {
            log.error("查询企业详情失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}
