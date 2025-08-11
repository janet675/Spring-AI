package com.github.zzzzbw.ai.controller;

import com.github.zzzzbw.ai.Service.FileService;
import com.github.zzzzbw.ai.entity.FileDocumentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/milvus2")
public class AIController {
    private static final Logger log = LoggerFactory.getLogger(AIController.class);

    Resource systemResource = new FileSystemResource("prompts/system-qa.st");
    Resource springAiResource = new FileSystemResource("data/AI.pdf");

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private OllamaChatModel chatModel;

    @Autowired
    FileService fileService;




    @PostMapping("/upload")
    public String upload(MultipartFile file) {

       String path=fileService.uploadFile(file);

       String file_name=file.getOriginalFilename();
       String type = file_name.substring(file_name.lastIndexOf("."));
       fileService.updocument(path,type,file_name);
       return path;


    }
    @PostMapping("/select")
    public List<FileDocumentInfo> select(@RequestParam(value = "name", required = false) String fileName,
                                         @RequestParam(value = "content",required = false) String content) {

       List<FileDocumentInfo> list1 = fileService.selectFileDocumentInfoByCondition(fileName, content);
       return list1;

    }

    @PostMapping("/delete")
    public int delete(@RequestParam("id") int id) {
       int reid= fileService.deleteFile(id);
       //返回删除id
       return reid;
    }

    @PostMapping("/update")
    public String update(@RequestParam("documentContent")  String documentContent,@RequestParam("id") String id ) {


       String doc=fileService.updatedocument(documentContent,id);
       return doc;


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
                .advisors(new QuestionAnswerAdvisor(vectorStore))
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
                .advisors(new QuestionAnswerAdvisor(vectorStore))
                .user(message)
                .stream()
                .chatResponse();
    }

}