package com.github.zzzzbw.ai.Dao;

import com.github.zzzzbw.ai.Service.FileService;
import com.github.zzzzbw.ai.entity.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.github.zzzzbw.ai.entity.Documentinfo;
import org.springframework.ai.vectorstore.VectorStore;


@Repository
public class FileDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;



    public void insertFile(FileInfo fileInfo) {
      String sql = "insert into SPRING_AI_FILE (file_name, file_path, type, file_size," +
              " upload_time, status ) values (?,?,?,?" +
              ",?,?)";
        Object args[] = {

               fileInfo.getFilename(),
               fileInfo.getFilePath(),
               fileInfo.getType(),
               fileInfo.getFileSize(),
               fileInfo.getUploadTime(),
               fileInfo.getStatus()
        };
          jdbcTemplate.update(sql,args);

    }

    public int selectid (String fileName) {
        String sql="select id from SPRING_AI_FILE  where file_name like  '%?%' ";
       int id=jdbcTemplate.queryForObject(sql,int.class,fileName);
       return id;

    }
    public void selectfile (String fileName) {
        String sql="select a.id,b.Document from SPRING_AI_FILE a join SPRING_AI_DOCUMENT b  where a.file_name like '%?%' and b.file_name like '%?%' ";
        jdbcTemplate.queryForObject(sql,int.class,String.class,fileName,fileName);


    }

    public void delateFile(int id){

        jdbcTemplate.update("delete from SPRING_AI_FILE where id = ?",id);
        //删除对应的所有doc
        jdbcTemplate.update("delete from SPRING_AI_DOCUMENT where file_id = ?",id);



    }

    public void updateFileStatus(String status,int id){
        String sql = "update SPRING_AI_FILE set status=? where file_id = ?";
        jdbcTemplate.update(sql,status,id);



    }


}
