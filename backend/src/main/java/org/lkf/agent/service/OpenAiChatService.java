package org.lkf.agent.service;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.lkf.agent.common.exception.BusinessException;
import org.lkf.agent.config.ChatProperties;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Service
public class OpenAiChatService {

    private final ChatProperties chatProperties;

    public OpenAiChatService(ChatProperties chatProperties) {
        this.chatProperties = chatProperties;
    }

    public String generate(String systemPrompt, String userPrompt) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.from(safeText(systemPrompt)));
        messages.add(UserMessage.from(safeText(userPrompt)));
        return generate(messages);
    }

    public String generate(List<ChatMessage> messages) {
        validateConfig();
        try {
            OpenAiChatModel chatModel = buildChatModel();
            ChatResponse response = chatModel.chat(messages);
            String answer = response == null || response.aiMessage() == null ? "" : response.aiMessage().text();
            if (answer == null || answer.isBlank()) {
                throw new BusinessException("对话模型响应内容为空");
            }
            return answer.trim();
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException("对话模型调用异常: " + exception.getMessage());
        }
    }

    public void streamGenerate(String systemPrompt, String userPrompt, Consumer<String> onToken,
                               Runnable onComplete, Consumer<Throwable> onError) {
        List<ChatMessage> messages = List.of(
                SystemMessage.from(safeText(systemPrompt)),
                UserMessage.from(safeText(userPrompt))
        );
        streamGenerate(messages, onToken, onComplete, onError);
    }

    public void streamGenerate(List<ChatMessage> messages, Consumer<String> onToken,
                               Runnable onComplete, Consumer<Throwable> onError) {
        validateConfig();
        try {
            String answer = generate(messages);
            if (onToken != null && answer != null && !answer.isBlank()) {
                onToken.accept(answer);
            }
            if (onComplete != null) {
                onComplete.run();
            }
        } catch (Exception exception) {
            if (onError != null) {
                onError.accept(exception);
                return;
            }
            throw new BusinessException("对话模型流式调用异常: " + exception.getMessage());
        }
    }

    public OpenAiChatModel buildChatModel() {
        validateConfig();
        return OpenAiChatModel.builder()
                .baseUrl(normalizeBaseUrl(chatProperties.getBaseUrl()))
                .apiKey(chatProperties.getApiKey().trim())
                .modelName(chatProperties.getModel().trim())
                .temperature(resolveTemperature())
                .maxTokens(resolveMaxTokens())
                .timeout(Duration.ofSeconds(resolveTimeoutSeconds()))
                .build();
    }

    public OpenAiStreamingChatModel buildStreamingChatModel() {
        validateConfig();
        return OpenAiStreamingChatModel.builder()
                .baseUrl(normalizeBaseUrl(chatProperties.getBaseUrl()))
                .apiKey(chatProperties.getApiKey().trim())
                .modelName(chatProperties.getModel().trim())
                .temperature(resolveTemperature())
                .maxTokens(resolveMaxTokens())
                .timeout(Duration.ofSeconds(resolveTimeoutSeconds()))
                .build();
    }

    private String normalizeBaseUrl(String baseUrl) {
        String base = safeText(baseUrl);
        if (base.endsWith("/v1")) {
            return base;
        }
        if (base.endsWith("/")) {
            return base + "v1";
        }
        return base + "/v1";
    }

    private void validateConfig() {
        if (chatProperties.getBaseUrl() == null || chatProperties.getBaseUrl().isBlank()) {
            throw new BusinessException("未配置Chat baseUrl");
        }
        if (chatProperties.getApiKey() == null || chatProperties.getApiKey().isBlank()) {
            throw new BusinessException("未配置Chat apiKey");
        }
        if (chatProperties.getModel() == null || chatProperties.getModel().isBlank()) {
            throw new BusinessException("未配置Chat model");
        }
    }

    private String safeText(String text) {
        return text == null ? "" : text.trim();
    }

    private int resolveTimeoutSeconds() {
        return chatProperties.getTimeoutSeconds() == null || chatProperties.getTimeoutSeconds() < 10 ? 60 : chatProperties.getTimeoutSeconds();
    }

    private int resolveMaxTokens() {
        return chatProperties.getMaxTokens() == null || chatProperties.getMaxTokens() <= 0 ? 2048 : chatProperties.getMaxTokens();
    }

    private double resolveTemperature() {
        return chatProperties.getTemperature() == null ? 0.3 : chatProperties.getTemperature();
    }
}
