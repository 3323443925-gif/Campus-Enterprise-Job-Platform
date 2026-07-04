package com.example.pojo;

import lombok.Data;

import java.util.List;

@Data
public class InterviewInfo {
        // 面试岗位名称
        private String jobPosition;
        // 岗位技术分类
        private String jobCategory;
        // 难度：初级/中级/高级/专家
        private String difficultyLevel;
        // 面试总时长 分钟
        private Integer totalTimeMinute;
        // 题型列表
        private List<String> questionTypes;
        // 核心考察知识点
        private List<String> examinePoints;
        // AI面试官风格：温和/标准/高压严厉/反问刁难
        private String interviewStyle;
        // 是否开启压力面试
        private Boolean openPressureTest;
}
