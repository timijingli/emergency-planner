package com.emergency.service;

import com.emergency.model.PlanResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 调用 DeepSeek API 的核心服务
 *
 * 流程：
 *   1. 拼装「系统提示词 + 用户场景」
 *   2. 通过 OkHttp 发送 POST 请求到 DeepSeek
 *   3. 解析返回的 JSON → PlanResponse 对象
 */
@Service
public class DeepSeekService {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekService.class);

    private final String apiKey;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    /** API 地址 */
    private static final String API_URL = "https://api.deepseek.com/chat/completions";

    /** 使用模型（deepseek-chat 性价比最高，deepseek-reasoner 推理更强） */
    private static final String MODEL = "deepseek-chat";

    public DeepSeekService(
            @Value("${deepseek.api-key}") String apiKey,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)   // AI 生成可能需要较长时间
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 根据场景 + 增强上下文生成应急预案
     *
     * @param scenario       用户输入的场景描述
     * @param enrichedContext 增强上下文（位置+天气+周边设施+用户附加信息），已由PlanController组装好
     */
    public PlanResponse generatePlan(String scenario, String enrichedContext) throws IOException {
        // 1. 拼装消息
        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(scenario, enrichedContext);

        // 2. 构造请求体
        Map<String, Object> requestBody = Map.of(
                "model", MODEL,
                "temperature", 0.7,        // 适当随机性
                "max_tokens", 4096,        // 足够长的输出
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
        );

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // 3. 发请求
        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "无错误详情";
                throw new IOException("DeepSeek API 返回错误 " + response.code() + ": " + errorBody);
            }

            String responseBody = response.body().string();

            // 4. 解析 DeepSeek 返回的 JSON 结构：
            //    { "choices": [{ "message": { "content": "{\"title\":\"...\"}" } }] }
            Map<String, Object> root = objectMapper.readValue(responseBody, Map.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) root.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new IOException("DeepSeek 没有返回任何内容，请重试");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");

            log.info("=== DeepSeek 原始返回内容（前500字符）===\n{}",
                     content != null ? content.substring(0, Math.min(500, content.length())) : "null");

            // 5. content 本身就是 JSON 字符串，反序列化为 PlanResponse
            //    清理可能的 markdown 代码块标记及额外文字
            content = cleanJsonContent(content);
            log.info("=== 清理后的JSON（前500字符）===\n{}",
                     content.substring(0, Math.min(500, content.length())));

            return objectMapper.readValue(content, PlanResponse.class);
        }
    }

    // ==================== 系统提示词 ====================

    private String buildSystemPrompt() {
        return """
                你是一名资深的国家级应急管理专家，拥有20年一线救援经验，曾参与过汶川地震、天津港爆炸等重大灾害救援。
                你的任务是根据用户提供的紧急场景、地理位置、实时天气、周边设施和灾害预警等信息，生成一份精准、分步骤、可操作的应急行动预案。

                ## 核心原则
                1. **生命安全永远是第一位**：所有建议的第一优先级是保护人的生命
                2. **可操作性**：每一条建议必须是具体可执行的，不说空话套话
                3. **分秒必争**：前30秒的行动最为关键，要单独列出
                4. **通俗易懂**：用普通人能理解的语言，避免过于专业的术语
                5. **贴合场景**：紧密围绕用户描述的具体场景给出建议
                6. **利用周边资源**：充分利用用户提供的周边安全设施（避难所、医院、消防站等）信息，在预案中明确指出方向和距离
                7. **考虑天气因素**：预案必须结合实时天气条件（如大雨影响逃生速度、大风增加火灾风险、低温增加失温风险等）
                8. **应对预警信号**：如果有灾害预警生效，预案必须针对性应对

                ## 输出格式要求
                请严格按照以下 JSON 格式输出，不要输出任何其他内容，不要用 markdown 代码块包裹：

                {
                  "title": "预案标题（简洁有力，如：暴雨夜宿舍楼火灾应急行动预案）",
                  "riskLevel": "高/中/低",
                  "overview": "用1-2句话清晰概述当前形势和核心威胁，必须提及天气和位置",
                  "firstActions": [
                    {"step": 1, "action": "第一时间要做的事", "reason": "简明扼要的原因说明"}
                  ],
                  "steps": [
                    {
                      "phase": "阶段名称（如：第一阶段——紧急避险（0-2分钟））",
                      "steps": [
                        {"step": 1, "action": "具体行动描述（如：向东南方向XX避难所撤离，约350米）", "reason": "为什么要这样做", "attention": "需要特别注意的安全事项（尤其天气相关）"}
                      ]
                    }
                  ],
                  "dontDo": ["绝对不能做的事项1", "绝对不能做的事项2"],
                  "afterRescue": "脱离危险后的处理建议，包括心理安抚、医疗检查、联系家人等",
                  "emergencyContacts": [
                    {"name": "火警", "number": "119"},
                    {"name": "报警", "number": "110"},
                    {"name": "急救", "number": "120"}
                  ]
                }

                请确保：
                - firstActions 列出前30秒最关键的行动（3-5条）
                - steps 分为2-4个阶段，每个阶段2-5个步骤，步骤中明确提及周边可用设施
                - dontDo 列出3-5条绝对禁止的行为，考虑天气条件
                - emergencyContacts 中除了通用紧急电话，还要根据场景补充相关机构的电话
                - 所有建议必须具体到当前场景、位置、天气，不要泛泛而谈
                """;
    }

    /**
     * 组装用户提示词：场景 + 增强上下文
     */
    private String buildUserPrompt(String scenario, String enrichedContext) {
        StringBuilder sb = new StringBuilder();
        sb.append("## 紧急场景\n");
        sb.append(scenario);

        if (enrichedContext != null && !enrichedContext.isBlank()) {
            sb.append("\n\n");
            sb.append(enrichedContext);
        }

        sb.append("\n\n请基于以上所有信息（场景、位置、天气、周边设施、灾害预警），立刻生成一份贴合实际情况的应急预案。");
        return sb.toString();
    }

    /**
     * 清理 AI 返回的 JSON 字符串 — 处理各种可能的包裹情况：
     *   1. "这是一份预案：\n```json\n{...}\n```"  → 有前缀文字 + markdown 包裹
     *   2. "```json\n{...}\n```"                  → 纯 markdown 包裹
     *   3. "```\n{...}\n```"                      → 无语言标记的 markdown
     *   4. "{...}"                                 → 纯 JSON（理想情况）
     *   5. "前言文字...\n{...}\n结尾文字..."        → JSON 被夹在普通文字中
     */
    private String cleanJsonContent(String content) {
        if (content == null || content.isBlank()) return "";

        // 第一步：提取 ``` 代码块中的内容（如果有的话）
        int codeBlockStart = content.indexOf("```json");
        if (codeBlockStart == -1) {
            codeBlockStart = content.indexOf("```");
        }
        if (codeBlockStart >= 0) {
            // 跳过开头标记行（含换行）
            int blockContentStart = content.indexOf('\n', codeBlockStart);
            if (blockContentStart < 0) {
                blockContentStart = codeBlockStart + 3;
                if (content.startsWith("```json", codeBlockStart)) {
                    blockContentStart = codeBlockStart + 7;
                }
            }
            int closingFence = content.indexOf("```", blockContentStart);
            if (closingFence >= 0) {
                content = content.substring(blockContentStart, closingFence).trim();
            } else {
                content = content.substring(blockContentStart).trim();
            }
        }

        // 第二步：如果没有代码块，找第一个 { 和最后一个 } 之间的 JSON
        int firstBrace = content.indexOf('{');
        int lastBrace = content.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            content = content.substring(firstBrace, lastBrace + 1);
        }

        return content.trim();
    }
}
