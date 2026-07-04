package com.example.controller;

import com.example.pojo.Result;
import com.example.utils.aliyunOSSOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/common")
public class CommonController {

    @Autowired
    private aliyunOSSOperator aliyunOSSOperator;

    /**
     * 通用文件上传（图片、PDF简历、营业执照），返回文件访问路径
     */
    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            log.info("文件上传: {}", originalFilename);

            String url = aliyunOSSOperator.upload(file.getBytes(), originalFilename);
            log.info("文件上传成功: {}", url);

            return Result.success(url);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}
