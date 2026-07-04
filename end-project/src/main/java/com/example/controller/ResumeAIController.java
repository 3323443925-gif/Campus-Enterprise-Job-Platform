package com.example.controller;

import com.example.pojo.JobDeliver;
import com.example.pojo.RecruitJob;
import com.example.pojo.Result;
import com.example.pojo.StudentResume;
import com.example.pojo.SysUser;
import com.example.service.JobDeliverService;
import com.example.service.RecruitJobService;
import com.example.service.StudentResumeService;
import com.example.service.SysUserService;
import com.example.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/resume-ai")
public class ResumeAIController {

    private final ChatClient chatClient;

    @Autowired
    private StudentResumeService studentResumeService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private JobDeliverService jobDeliverService;

    @Autowired
    private RecruitJobService recruitJobService;

    private static final String BASE_RESUME_PROMPT = """
            你是一位资深的HR招聘助手，专业帮助企业HR筛选和评估求职者简历。
            
            【你的核心职责】
            1. 深入分析求职者简历，精准匹配岗位需求；
            2. 快速识别候选人核心优势和潜在风险；
            3. 为HR提供专业的录用决策建议；
            
            【分析原则】
            ✅ 严格基于提供的简历内容和岗位要求进行分析，绝不编造信息；
            ✅ 以岗位匹配度为核心，重点关注技能、经验与岗位需求的契合度；
            ✅ 客观公正，不被主观因素影响；
            ✅ 突出重点，用简洁的语言呈现关键信息；
            
            【输出格式要求】
            使用纯文本格式输出，结构清晰：
            - 重要结论用**加粗**突出
            - 优势用✅标记，风险用⚠️标记
            - 使用列表和分段，便于快速阅读
            - 关键数据用数字或百分比呈现
            - 适当的换行
            
            【禁止行为】
            ❌ 不询问与简历分析无关的问题；
            ❌ 不凭空猜测简历中未提及的信息；
            ❌ 不给出过于主观或情绪化的评价；
            """;

    private static final String SUMMARY_PROMPT = """
            请输出一份完整的简历分析报告，严格按照以下结构输出：
            
            ## 📋 候选人基本信息
            - 姓名：xxx
            - 学号：xxx
            - 专业：xxx
            - 期望城市：xxx
            - 期望薪资：xxx
            
            ## 🏢 投递岗位信息
            - 岗位名称：xxx
            - 岗位类别：xxx
            - 薪资范围：xxx
            - 学历要求：xxx
            - 工作地点：xxx
            
            ## ⚖️ 岗位匹配度分析
            ### ✅ 核心优势（与岗位匹配的亮点）
            1. xxx
            2. xxx
            
            ### ⚠️ 潜在不足（与岗位要求的差距）
            1. xxx
            2. xxx
            
            ## 📊 能力维度评分（满分100分）
            | 维度 | 评分 | 说明 |
            |------|------|------|
            | 教育背景匹配度 | xx分 | xxx |
            | 专业技能匹配度 | xx分 | xxx |
            | 实习/项目经验 | xx分 | xxx |
            | 综合素质 | xx分 | xxx |
            | **综合评分** | **xx分** | xxx |
            
            ## 🎯 HR决策建议
            **推荐等级**：【强烈推荐 / 推荐 / 待定 / 不推荐】
            
            **面试关注点**：
            1. xxx
            2. xxx
            
            **风险提示**（如有）：
            - xxx
            """;

    public ResumeAIController(ChatClient.Builder builder) {
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();

        this.chatClient = builder
                .defaultSystem(BASE_RESUME_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @GetMapping("/resume/{deliverId}")
    public Result getResumeDetail(@PathVariable Long deliverId, HttpServletRequest request) {
        try {
            Long hrUserId = getCurrentUserId(request);
            if (hrUserId == null) {
                return Result.error("未登录");
            }

            JobDeliver jobDeliver = jobDeliverService.findById(deliverId);
            if (jobDeliver == null) {
                return Result.error("投递记录不存在");
            }

            Long resumeId = jobDeliver.getResumeId();
            if (resumeId == null) {
                return Result.error("简历不存在");
            }

            StudentResume resume = studentResumeService.findById(resumeId);
            if (resume == null) {
                return Result.error("简历不存在");
            }

            SysUser student = sysUserService.findById(resume.getStudentUserId());
            if (student == null) {
                return Result.error("学生信息不存在");
            }

            Map<String, Object> data = new HashMap<>();
            data.put("resume", resume);
            data.put("studentName", student.getRealName());
            data.put("studentNo", resume.getStudentNo());
            data.put("username", student.getUsername());
            data.put("deliverId", deliverId);

            return Result.success(data);
        } catch (Exception e) {
            log.error("获取简历详情失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAnalyze(
            @RequestParam String message,
            @RequestParam(defaultValue = "default") String sessionId,
            @RequestParam Long deliverId,
            HttpServletRequest request
    ) {
        Long hrUserId = getCurrentUserId(request);
        if (hrUserId == null) {
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new RuntimeException("未登录"));
            return emitter;
        }

        JobDeliver jobDeliver = jobDeliverService.findById(deliverId);
        if (jobDeliver == null) {
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new RuntimeException("投递记录不存在"));
            return emitter;
        }

        Long resumeId = jobDeliver.getResumeId();
        if (resumeId == null) {
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new RuntimeException("简历不存在"));
            return emitter;
        }

        StudentResume resume = studentResumeService.findById(resumeId);
        if (resume == null) {
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new RuntimeException("简历不存在"));
            return emitter;
        }

        SysUser student = sysUserService.findById(resume.getStudentUserId());
        
        RecruitJob job = recruitJobService.findById(jobDeliver.getJobId());

        String pdfContent = "";
        if (resume.getResumeFile() != null && !resume.getResumeFile().isEmpty()) {
            try {
                pdfContent = extractTextFromPDF(resume.getResumeFile());
            } catch (Exception e) {
                log.warn("解析PDF失败，将使用文本字段内容进行分析", e);
                pdfContent = "PDF解析失败: " + e.getMessage();
            }
        }

        SseEmitter emitter = new SseEmitter(180_000L);
        String resumeContext = buildResumeContext(resume, student, job, pdfContent);

        chatClient.prompt()
                .system(resumeContext)
                .user(message)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, sessionId))
                .stream()
                .content()
                .subscribe(
                        token -> {
                            try {
                                emitter.send(token);
                            } catch (Exception e) {
                                emitter.completeWithError(e);
                            }
                        },
                        emitter::completeWithError,
                        emitter::complete
                );
        return emitter;
    }

    @PostMapping("/summary")
    public Result generateSummary(
            @RequestParam String sessionId,
            @RequestParam Long deliverId,
            HttpServletRequest request
    ) {
        Long hrUserId = getCurrentUserId(request);
        if (hrUserId == null) {
            return Result.error("未登录");
        }

        JobDeliver jobDeliver = jobDeliverService.findById(deliverId);
        if (jobDeliver == null) {
            return Result.error("投递记录不存在");
        }

        Long resumeId = jobDeliver.getResumeId();
        if (resumeId == null) {
            return Result.error("简历不存在");
        }

        StudentResume resume = studentResumeService.findById(resumeId);
        if (resume == null) {
            return Result.error("简历不存在");
        }

        SysUser student = sysUserService.findById(resume.getStudentUserId());
        
        RecruitJob job = recruitJobService.findById(jobDeliver.getJobId());

        String pdfContent = "";
        if (resume.getResumeFile() != null && !resume.getResumeFile().isEmpty()) {
            try {
                pdfContent = extractTextFromPDF(resume.getResumeFile());
            } catch (Exception e) {
                log.warn("解析PDF失败，将使用文本字段内容进行分析", e);
                pdfContent = "PDF解析失败: " + e.getMessage();
            }
        }

        String resumeContext = buildResumeContext(resume, student, job, pdfContent);
        String fullSystemPrompt = resumeContext + "\n" + SUMMARY_PROMPT;

        String report = chatClient.prompt()
                .system(fullSystemPrompt)
                .user("请生成完整的简历分析报告")
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, sessionId))
                .call()
                .content();

        return Result.success(report);
    }

    private String extractTextFromPDF(String pdfUrl) throws Exception {
        URL url = new URL(pdfUrl);
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(60000);

        try (InputStream inputStream = new BufferedInputStream(connection.getInputStream())) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            byte[] pdfBytes = baos.toByteArray();

            try (PDDocument document = Loader.loadPDF(pdfBytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);

                if (text == null || text.trim().isEmpty()) {
                    return "PDF文件内容为空或无法提取";
                }

                return text;
            }
        }
    }

    private String buildResumeContext(StudentResume resume, SysUser student, RecruitJob job, String pdfContent) {
        String studentName = student != null ? student.getRealName() : "未知";
        String email = student != null ? student.getEmail() : "未知";
        String phone = student != null ? student.getPhone() : "未知";
        
        String jobName = job != null ? job.getJobName() : "未知";
        String jobCategory = job != null ? job.getJobCategory() : "未知";
        String salaryRange = job != null ? (job.getSalaryMin() + "-" + job.getSalaryMax()) : "未知";
        String educationRequire = job != null ? job.getEducationRequire() : "未知";
        String workAddress = job != null ? job.getWorkAddress() : "未知";
        String jobRequire = job != null ? job.getJobRequire() : "未知";
        String jobDesc = job != null ? job.getJobDesc() : "未知";

        return """
                ==========投递岗位信息==========
                岗位名称：%s
                岗位类别：%s
                薪资范围：%s
                学历要求：%s
                工作地点：%s
                岗位要求：%s
                岗位描述：%s
                
                ==========候选人简历信息==========
                候选人姓名：%s
                学号：%s
                邮箱：%s
                联系电话：%s
                期望工作城市：%s
                期望薪资：%d
                所学专业ID：%d
                自我评价：%s
                教育经历：%s
                实习经历：%s
                技能证书：%s
                
                ==========PDF简历文件内容==========
                %s
                
                请基于以上简历内容和岗位要求进行专业的岗位匹配度分析，优先参考PDF文件中的内容。
                """.formatted(
                jobName,
                jobCategory,
                salaryRange,
                educationRequire,
                workAddress,
                jobRequire,
                jobDesc,
                studentName,
                resume.getStudentNo() != null ? resume.getStudentNo() : "未知",
                email,
                phone,
                resume.getExpectCity() != null ? resume.getExpectCity() : "未填写",
                resume.getExpectSalary() != null ? resume.getExpectSalary() : 0,
                resume.getMajorId() != null ? resume.getMajorId() : 0,
                resume.getSelfIntro() != null ? resume.getSelfIntro() : "未填写",
                resume.getEducationExp() != null ? resume.getEducationExp() : "未填写",
                resume.getPracticeExp() != null ? resume.getPracticeExp() : "未填写",
                resume.getCerts() != null ? resume.getCerts() : "未填写",
                pdfContent != null && !pdfContent.isEmpty() ? pdfContent : "未上传PDF简历或解析失败"
        );
    }

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