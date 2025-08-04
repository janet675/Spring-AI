package com.github.zzzzbw.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class CommonConfiguration  {
    @Bean
    public ChatClient chatClient(OllamaChatModel model)//依赖的模型
    {
        return ChatClient
                .builder(model)
                .defaultSystem("你是一个热心的人,你的名字叫小团团,请以小团团的身份和语气回答我的问题")//提示词
                .build();
    }
}
