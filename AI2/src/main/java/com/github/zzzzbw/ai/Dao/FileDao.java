package com.github.zzzzbw.ai.Dao;

import com.github.zzzzbw.ai.Service.FileService;
import com.github.zzzzbw.ai.entity.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.github.zzzzbw.ai.entity.Documentinfo;
import org.springframework.ai.vectorstore.VectorStore;

import java.sql.ResultSet;
import java.sql.SQLException;


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
        String sql="select id from SPRING_AI_FILE  where file_name = ?";
       int id=jdbcTemplate.queryForObject(sql,int.class,fileName);
       System.out.println(id);
       return id;

    }
    public void selectfile (String fileName) {
        String sql="select a.id,b.Document from SPRING_AI_FILE a join SPRING_AI_DOCUMENT b  where a.file_name like '%?%' and b.file_name like '%?%' ";

        FileInfo fileresult=jdbcTemplate.queryForObject(sql, new RowMapper<FileInfo>() {
            @Override
            public FileInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                   FileInfo fileInfo=new FileInfo();
                   fileInfo.setId(rs.getInt("id"));
                   fileInfo.setFilename(rs.getString("file_name"));
                   fileInfo.setFilePath(rs.getString("file_path"));
                   fileInfo.setType(rs.getString("type"));
                   fileInfo.setFileSize(rs.getLong("file_size"));
                   fileInfo.setUploadTime(rs.getTimestamp("upload_time"));
                   fileInfo.setStatus(rs.getString("status"));
                   return fileInfo;
            }
        }, fileName,fileName);
        System.out.println(fileresult);


    }

    public void delateFile(int id){

        String sql="delete from SPRING_AI_FILE where id = ?";
        int rows=jdbcTemplate.update(sql,id);
        //删除对应的所有doc
        System.out.println(rows);



    }

    public void updateFileStatus(String status,int id){
        String sql = "update SPRING_AI_FILE set status=? where file_id = ?";
        int rows=jdbcTemplate.update(sql,status,id);
        System.out.println(rows);



    }


}
