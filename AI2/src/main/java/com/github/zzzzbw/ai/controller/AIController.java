package com.github.zzzzbw.ai.controller;

import com.github.zzzzbw.ai.Service.FileService;
import com.github.zzzzbw.ai.entity.FileDocumentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
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
    @Autowired
   JdbcChatMemoryRepository  chatMemoryRepository;






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
    @PostMapping("/ask")
    public String ask(@RequestParam("message") String message) throws IOException {



        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();



        var qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder().similarityThreshold(0.4d).topK(3).build())
                .build();



        return ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build().prompt()

                .advisors(qaAdvisor)
                .user(message)
                .call()
                .content();





    }




}