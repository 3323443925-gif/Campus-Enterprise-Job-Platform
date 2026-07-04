package com.example.controller;

import com.example.pojo.InterviewInfo;
import com.example.pojo.Result;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI模拟面试流式对话控制器
 * 1. POST /stream SSE流式面试问答，JSON传递完整面试配置
 * 2. POST /finish 结束面试，生成完整评测总结报告
 */
@RestController
@RequestMapping("/api/interview")
public class InterviewChatController {

    private final ChatClient chatClient;

    // 基础面试系统提示词
    private static final String BASE_INTERVIEW_PROMPT = """
            你现在是一名专业技术面试官，严格按照传入的面试配置完成整场模拟面试。
            【基础面试规则】
            1. 全程围绕目标岗位提问，禁止聊无关话题；
            2. 提问循序渐进：基础知识点 → 项目深挖 → 情景/代码题；
            3. 候选人回答后针对性追问，主动挖掘知识盲区；
            4. 单次只抛出一个问题，不要一次性多问；
            5. 面试结束后可根据要求输出完整评测总结。
            
            【禁止行为】
            1. 不主动直接给出标准答案，引导候选人自主思考；
            2. 不得随意降低面试难度，严格匹配配置难度等级；
            3. 所有传入核心考点必须逐一覆盖提问；
            4. 候选人跑题时温和拉回面试主线。
            """;

    // 面试结束生成报告专用提示词
    private static final String REPORT_PROMPT = """
            本场面试已全部结束，你需要输出一份完整、客观的面试评测报告，结构如下：
            1. 综合得分（满分100）
            2. 候选人优势亮点
            3. 薄弱知识点与回答漏洞
            4. 针对性学习提升建议
            5. 岗位适配度结论（推荐录用/待定/不推荐）
            要求客观严谨，结合整场对话回答内容分析，不要空话套话。
            """;

    public InterviewChatController(ChatClient.Builder builder) {
        // 内存对话记忆，滑动窗口最多保存20轮问答，防止token溢出
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();

        this.chatClient = builder
                .defaultSystem(BASE_INTERVIEW_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    /**
     * 流式面试问答接口（POST+JSON传面试配置，优化方案A）
     * @param message 候选人回答/开场白
     * @param sessionId 会话唯一ID，隔离不同面试上下文
     * @param interviewConfig 完整面试配置JSON对象
     * @return SSE流式逐字输出面试官提问
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamInterview(
            @RequestParam String message,
            @RequestParam(defaultValue = "default") String sessionId,
            @RequestBody InterviewInfo interviewConfig
    ) {
        // SSE连接超时3分钟
        SseEmitter emitter = new SseEmitter(180_000L);
        // 拼接本场面试动态规则
        String dynamicRule = buildDynamicInterviewPrompt(interviewConfig);

        chatClient.prompt()
                .system(dynamicRule)
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

    /**
     * 面试结束接口（优化方案C）：生成完整面试评测报告
     * @param sessionId 面试会话ID
     * @param interviewConfig 本场面试配置
     * @return 完整面试总结报告文本
     */
    @PostMapping("/finish")
    public Result finishInterview(
            @RequestParam String sessionId,
            @RequestBody InterviewInfo interviewConfig
    ) {
        String dynamicRule = buildDynamicInterviewPrompt(interviewConfig);
        // 组合基础规则+本场配置+报告生成指令
        String fullSystemPrompt = dynamicRule + "\n" + REPORT_PROMPT;

        String report = chatClient.prompt()
                .system(fullSystemPrompt)
                .user("面试全部结束，请生成完整面试评测报告")
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, sessionId))
                .call()
                .content();
        
        return Result.success(report);
    }

    /**
     * 根据AiInterview配置生成动态面试规则Prompt
     */
    private String buildDynamicInterviewPrompt(InterviewInfo config) {
        return """
                ==========本场面试专属配置==========
                面试岗位：%s
                技术分类：%s
                面试难度：%s
                面试官风格：%s
                是否压力面试：%s
                限时总时长：%d分钟
                题型要求：%s
                
                【风格执行细则】
                1. 压力面试开启：连环追问、质疑回答漏洞、短促反问，不主动引导；
                2. 温和风格：耐心引导，回答错误平缓提示，无刻意刁难；
                3. 严格按照以上全部配置执行完整面试流程。
                """.formatted(
                config.getJobPosition(),
                config.getJobCategory(),
                config.getDifficultyLevel(),
                config.getInterviewStyle(),
                config.getOpenPressureTest() ? "是" : "否",
                config.getTotalTimeMinute(),
                config.getQuestionTypes() != null ? String.join("、", config.getQuestionTypes()) : "未指定"
        );
    }
}