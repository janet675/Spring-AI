package com.github.zzzzbw.ai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class ChatController {
    private final ChatClient chatClient;//自动注入
    @RequestMapping(value = "chat",produces = "text/html;charset=utf-8")
    public Flux<String> chat(String prompt){
        // 链式调用：构建提示 -> 发送请求 -> 获取文本响应
        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }
}
