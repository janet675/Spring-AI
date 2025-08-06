package com.github.zzzzbw.ai.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import java.io.IOException;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/milvus2")
public class AIController {
    private static final Logger log = LoggerFactory.getLogger(AIController.class);

    Resource systemResource=new FileSystemResource("prompts/system-qa.st");

    Resource springAiResource=new FileSystemResource("data/AI.pdf");

    @Autowired
    @Qualifier("vectorStore2")
    private VectorStore vectorStore;

    @Autowired
    private  OllamaChatModel chatModel;



    /**
     * 处理PDF文档的解析、分割和嵌入存储。
     * 使用 PagePdfDocumentReader 解析PDF文档并生成 Document 列表。
     * 使用 TokenTextSplitter 将文档分割成更小的部分。
     * 将分割后的文档添加到向量存储中，以便后续检索和生成。
     */
    @GetMapping("/insertDocuments")
    public void insertDocuments() throws IOException {
        // 1. parse document
        DocumentReader reader = new PagePdfDocumentReader(springAiResource);
        List<Document> documents = reader.get();
        log.info("{} documents loaded", documents.size());

        // 2. split trunks
        List<Document> splitDocuments = new TokenTextSplitter().apply(documents);
        log.info("{} documents split", splitDocuments.size());

        // 3. create embedding and store to vector store
        log.info("create embedding and save to vector store");
        vectorStore.add(splitDocuments);
    }

    /**
     * 根据用户输入的消息生成JSON格式的聊天响应。
     * 创建一个 SearchRequest 对象，设置返回最相关的前2个结果。
     * 从 systemResource 中读取提示模板。
     * 使用 ChatClient 构建聊天客户端，调用 RetrievalRerankAdvisor 进行检索和重排序，并生成最终的聊天响应内容。
     */
    @GetMapping(value = "/ragJsonText", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public String ragJsonText(@RequestParam(value = "message",
            defaultValue = "如何使用spring ai alibaba?") String message) throws IOException {

        SearchRequest searchRequest = SearchRequest.builder().topK(2).build();

        String promptTemplate = systemResource.getContentAsString(StandardCharsets.UTF_8);

        return ChatClient.builder(chatModel)

                .build()
                .prompt()
                .user(message)
                .call()
                .content();
    }

    /**
     * 根据用户输入的消息生成流式聊天响应。
     * 类似于 ragJsonText 方法，但使用 stream() 方法以流的形式返回聊天响应。
     * 返回类型为 Flux<ChatResponse>，适合需要实时更新的场景。
     */
    @GetMapping(value = "/ragStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> ragStream(@RequestParam(value = "message",
            defaultValue = "如何使用spring ai alibaba?") String message) throws IOException {

        SearchRequest searchRequest = SearchRequest.builder().topK(2).build();

        String promptTemplate = systemResource.getContentAsString(StandardCharsets.UTF_8);

        return ChatClient.builder(chatModel)

                .build()
                .prompt()
                .user(message)
                .stream()
                .chatResponse();
    }

}