package com.github.zzzzbw.ai.Service;

import java.io.File;

import com.github.zzzzbw.ai.Dao.DocumentDao;
import com.github.zzzzbw.ai.Dao.FileDao;
import com.github.zzzzbw.ai.Dao.FileDao;
import com.github.zzzzbw.ai.entity.Documentinfo;
import com.github.zzzzbw.ai.entity.FileInfo;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;

import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.awt.SystemColor.TEXT;
import static java.awt.SystemColor.text;

@Service
public class FileService {

    private VectorStore vectorStore;

    private FileDao fileDao;

    private DocumentDao documentDao;

    List<Document> documents;



    public FileService(VectorStore vectorStore, FileDao fileDao, DocumentDao documentDao) {
        this.vectorStore = vectorStore;
        this.fileDao = fileDao;
        this.documentDao=documentDao;


    }

    public String uploadFile(MultipartFile multipartFile) {
        //1、文件类型是否可以上传
        FileInfo fileInfo = new FileInfo();

        //2、取出原始文件名，获取文件后缀
        int id=1;
        String filename = multipartFile.getOriginalFilename();
        long filesize = multipartFile.getSize();
        String fileType = filename.substring(filename.lastIndexOf("."));
        if (!".txt".equalsIgnoreCase(fileType) && !".pdf".equalsIgnoreCase(fileType)&&!".doc".equalsIgnoreCase(fileType)){

            String Houzhui=fileType;
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
        Date date= new Date();
        fileInfo.setUploadTime(date);
        fileInfo.setStatus("1");
        fileDao.insertFile(fileInfo);

        //4.1、解析文档


        //5、返回文件信息
        return filePath;
    }

    public Object updocument(String filePath,String fileType,String fileName) {

        //1、判断文件是否存在
           FileDao fileDao=new FileDao();

            int file_id;
            file_id=fileDao.selectid(fileName);
        //2、解析文档
        Resource springAiResource=new FileSystemResource(filePath);



        //异常
        //3、存向量
        String document_text=null;
        String id=null;

        if(fileType.equals("txt")){
            TextReader textReader = new TextReader(springAiResource);
            documents  = textReader.get();
            vectorStore.add(documents);
             document_text=documents.get(0).getText();
             id=documents.get(0).getId();
        }
        else if(fileType.equals("pdf")){
            DocumentReader reader = new PagePdfDocumentReader(springAiResource);
            documents = reader.get();
            vectorStore.add(documents);
             document_text=documents.get(0).getText();
            id=documents.get(0).getId();
            System.out.println("Error");

        }
        else if(fileType.equals("doc")){
            TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(springAiResource);
            documents= tikaDocumentReader.get();
            vectorStore.add(documents);
            document_text=documents.get(0).getText();
            id=documents.get(0).getId();

            System.out.println("sccuess");

        }




        //4、存mysql库
        Documentinfo documentinfo = new Documentinfo();
        documentinfo.setId(id);
        documentinfo.setFile_name(fileName);
        documentinfo.setFile_id(file_id);
        documentinfo.setDocument(document_text);
        documentinfo.setStatus("1");
        documentDao.insertDocuemnt(documentinfo);





        return null;
    }
    public  Object delatefile(int file_id) {
        FileDao fileDao=new FileDao();
        fileDao.delateFile(file_id);
        DocumentDao documentDao=new DocumentDao();
        //根据文件id找到对应的所有doc id
       String id=documentDao.selectDocumentID(file_id);
      // 删除所有的doc
        vectorStore.delete(id);
        return null;

    }

    public Object delatedocument(String document_id) {
        //根据文档id删除对应表和向量库
        DocumentDao documentDao=new DocumentDao();
        documentDao.delatedocument(document_id);
        vectorStore.delete(document_id);

        return null;
    }

    public Object updatedocument(String document_id,String file_path,String fileType) {

        Resource springAiResource=new FileSystemResource(file_path);
        String document_text=null;
        if(fileType.equals("txt")){
            TextReader textReader = new TextReader(springAiResource);
            documents  = textReader.get();
            vectorStore.add(documents);
            document_text=documents.get(0).getText();

        }
        else if(fileType.equals("pdf")){
            DocumentReader reader = new PagePdfDocumentReader(springAiResource);
            documents = reader.get();
            vectorStore.add(documents);
            document_text=documents.get(0).getText();



        }
        else if(fileType.equals("doc")){
            TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(springAiResource);
            documents= tikaDocumentReader.get();
            vectorStore.add(documents);
            document_text=documents.get(0).getText();




        }
        //根据文档id修改文本内容并修改向量库
        FileDao fileDao=new FileDao();
        documentDao.updatedocument(document_id,document_text);
        vectorStore.delete(document_id);

        vectorStore.add(documents);
        return null;
    }

}
