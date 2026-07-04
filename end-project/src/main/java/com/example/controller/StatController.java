package com.example.controller;

import com.example.pojo.Result;
import com.example.service.JobDeliverService;
import com.example.service.RecruitJobService;
import com.example.service.SysUserService;
import com.example.service.EnterpriseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据统计大屏接口控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/stat")
public class StatController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private JobDeliverService jobDeliverService;

    @Autowired
    private RecruitJobService recruitJobService;

    @Autowired
    private EnterpriseService enterpriseService;

    /**
     * 全局就业总览（总就业率、分专业就业率、总岗位数、薪资分布、热门岗位、企业分布）
     */
    @GetMapping("/overview")
    public Result overview() {
        try {
            int totalStudents = sysUserService.countByCondition("student", null, null);

            int employedStudents = jobDeliverService.countEmployedStudents();

            double employmentRate = 0;
            if (totalStudents > 0) {
                employmentRate = BigDecimal.valueOf(employedStudents)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalStudents), 2, RoundingMode.HALF_UP)
                        .doubleValue();
            }

            int totalJobs = recruitJobService.countTotalJobs();

            int totalEnterprises = enterpriseService.countTotal();

            List<Map<String, Object>> majorStats = sysUserService.findMajorEmploymentStats();

            List<Map<String, Object>> salaryStats = jobDeliverService.findSalaryStats();

            List<Map<String, Object>> jobStats = recruitJobService.findJobCategoryStats();

            List<Map<String, Object>> enterpriseStats = enterpriseService.findIndustryStats();

            Map<String, Object> data = new HashMap<>();
            data.put("totalStudents", totalStudents);
            data.put("employedStudents", employedStudents);
            data.put("employmentRate", employmentRate);
            data.put("totalJobs", totalJobs);
            data.put("totalEnterprises", totalEnterprises);
            data.put("majorStats", majorStats);
            data.put("salaryStats", salaryStats);
            data.put("jobStats", jobStats);
            data.put("enterpriseStats", enterpriseStats);

            log.info("查询全局就业总览成功: totalStudents={}, employedStudents={}, employmentRate={}%, totalJobs={}, totalEnterprises={}",
                    totalStudents, employedStudents, employmentRate, totalJobs, totalEnterprises);
            return Result.success(data);
        } catch (Exception e) {
            log.error("查询全局就业总览失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}
