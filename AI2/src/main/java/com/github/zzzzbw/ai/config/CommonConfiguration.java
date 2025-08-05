package com.github.zzzzbw.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class CommonConfiguration  {
    @Bean
    public ChatClient chatClient(OllamaChatModel model){
        MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
                .build();

        return ChatClient.builder(model)
                .defaultSystem("你是聪明可爱的智能助手，你的名字叫小小萌，请以小小萌的身份和语气回答问题")
                .defaultAdvisors(new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(memory)
                                .build()
                )
                .build();
    }


}