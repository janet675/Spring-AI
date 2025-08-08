package com.github.zzzzbw.ai.controller;

import com.github.zzzzbw.ai.Dao.FileDao;
import com.github.zzzzbw.ai.Service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.*;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

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
    @Autowired
    FileDao fileDao;




    @PostMapping("/upload")
    public void upload(MultipartFile file) {

       String path=fileService.uploadFile(file);

       String file_name=file.getOriginalFilename();
       String type = file_name.substring(file_name.lastIndexOf("."));
       fileService.updocument(path,type,file_name);


    }
    @PostMapping("/select")
    public void select() {
        Scanner sc=new Scanner(System.in);
        String fileName=sc.next();
        fileDao.selectid(fileName);

    }


    /**
     * 处理PDF文档的解析、分割和嵌入存储。
     * 使用 PagePdfDocumentReader 解析PDF文档并生成 Document 列表。
     * 使用 TokenTextSplitter 将文档分割成更小的部分。
     * 将分割后的文档添加到向量存储中，以便后续检索和生成。
     */
    @GetMapping("/insertDocuments")
    public void insertDocuments() throws IOException, SQLException {
        // 1. parse document

        StringBuilder text = new StringBuilder();
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("AI.pdf");
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {


            String line;
            while ((line = bufferedReader.readLine()) != null) {
                text.append(line);
            }
        }

        vectorStore.write(Arrays.stream(text.toString().split("\n")).map(Document::new).toList());

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