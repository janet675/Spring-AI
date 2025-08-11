package com.github.zzzzbw.ai.Service;

import java.io.*;

import com.github.zzzzbw.ai.Dao.DocumentDao;
import com.github.zzzzbw.ai.Dao.FileDao;
import com.github.zzzzbw.ai.entity.Documentinfo;
import com.github.zzzzbw.ai.entity.FileDocumentInfo;
import com.github.zzzzbw.ai.entity.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

@Service
public class FileService {

    private VectorStore vectorStore;

    private FileDao fileDao;

    private DocumentDao documentDao;

    private final Logger logger = LoggerFactory.getLogger(FileService.class);


    public FileService(VectorStore vectorStore, FileDao fileDao, DocumentDao documentDao) {
        this.vectorStore = vectorStore;
        this.fileDao = fileDao;
        this.documentDao = documentDao;


    }

    public String uploadFile(MultipartFile multipartFile) {
        //1、文件类型是否可以上传
        FileInfo fileInfo = new FileInfo();
//
        //2、取出原始文件名，获取文件后缀
        int id = 1;
        String filename = multipartFile.getOriginalFilename();
        long filesize = multipartFile.getSize();
        String fileType = filename.substring(filename.lastIndexOf("."));
        if (!".txt".equalsIgnoreCase(fileType) && !".pdf".equalsIgnoreCase(fileType) && !".doc".equalsIgnoreCase(fileType)) {

            String Houzhui = fileType;
        }


        //3、文件保存到服务器
        String filePath = "F:\\AIfile\\" + filename;
        File localFile = new File(filePath);
        try {
            multipartFile.transferTo(localFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        //4、报文件信息保存到数据库

        fileInfo.setFilename(filename);
        fileInfo.setFilePath(filePath);
        fileInfo.setType(fileType);
        fileInfo.setFileSize(filesize);
        Date date = new Date();
        fileInfo.setUploadTime(date);
        fileInfo.setStatus("1");
        fileDao.insertFile(fileInfo);

        //4.1、解析文档


        //5、返回文件信息
        return filePath;
    }

    public Object updocument(String filePath, String fileType, String fileName) {

        //1、判断文件是否存在
        int file_id = fileDao.selectid(fileName);


        //2、解析文档
        Resource springAiResource = new FileSystemResource(filePath);


        //异常
        //3、存向量
        String document_text = null;
        String id = null;

        if (fileType.equals(".txt")) {
            TextReader textReader = new TextReader(springAiResource);
            List<Document> documents = textReader.get();
            vectorStore.add(documents);
            document_text = documents.get(0).getText();
            id = documents.get(0).getId();
        } else if (fileType.equals(".pdf")) {
            DocumentReader reader = new PagePdfDocumentReader(springAiResource);
            List<Document> documents = reader.get();
            vectorStore.add(documents);
            document_text = documents.get(0).getText();
            id = documents.get(0).getId();
            System.out.println("Error");

        } else if (fileType.equals(".doc")) {
            TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(springAiResource);
            List<Document> documents = tikaDocumentReader.get();
            vectorStore.add(documents);
            document_text = documents.get(0).getText();
            id = documents.get(0).getId();

            System.out.println("sccuess");

        }

        System.out.println(id);


        //4、存mysql库
        Documentinfo documentinfo = new Documentinfo();
        documentinfo.setId(id);
        documentinfo.setFile_id(file_id);
        documentinfo.setFile_name(fileName);
        documentinfo.setDocument(document_text);
        documentinfo.setStatus("1");
        documentDao.insertDocuemnt(documentinfo);


        return document_text;
    }

    public int deleteFile(int fileId) {
        logger.debug("deleteFile file_id:{}", fileId);
        fileDao.delateFile(fileId);
        //根据文件id找到对应的所有doc id
        List<String> list = documentDao.selectDocumentID(fileId);
        int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            String id = list.get(0);
            documentDao.delateDocumentByFileId(id);
        }


        // 删除所有的doc
        vectorStore.delete(list);
        return fileId;

    }

    public String delatedocument(String document_id) {
        //根据文档id删除对应表和向量库
        DocumentDao documentDao = new DocumentDao();
        documentDao.delateDocumentByFileId(document_id);
        vectorStore.delete(document_id);

        return document_id;
    }

    public String updatedocument(String document_text, String id) {


        //根据文档id修改文本内容并修改向量库


        documentDao.updatedocument(document_text,id);
         List<String>list=new ArrayList<>();
         list.add(id);
        vectorStore.delete(list);
        Map<String,Object>metada=new HashMap<String,Object>();
        Document doc = new Document(id,document_text,metada);
        List<Document> documents = new ArrayList<>();
        documents.add(doc);
        vectorStore.add(documents);
        return document_text;






    }

    public List<FileDocumentInfo> selectFileDocumentInfoByCondition(String fileName, String content) {
        return fileDao.selectFileDocumentInfoByCondition(fileName, content);
    }
}
