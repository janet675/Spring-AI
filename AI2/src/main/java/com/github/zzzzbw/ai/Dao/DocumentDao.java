package com.github.zzzzbw.ai.Dao;
import com.github.zzzzbw.ai.entity.Documentinfo;
import com.github.zzzzbw.ai.entity.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class DocumentDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void insertDocuemnt(Documentinfo documentinfo) {
        String sql = "insert into SPRING_AI_DOCUMENT(id, file_name, file_id,document, status)values(?,?,?,?)";
        Object args[] = {
                documentinfo.getId(),
                documentinfo.getFile_name(),
                documentinfo.getFile_id(),
                documentinfo.getDocument(),
                documentinfo.getStatus()

        };
        System.out.println("success1");
        jdbcTemplate.update(sql,args);
    }
    public void selectDocument(String doc) {
        String sql="select id,Document from SPRING_AI_DOCUMENT where Document like '%?%'";
        Documentinfo documentresult=jdbcTemplate.queryForObject(sql, new RowMapper<Documentinfo>() {
            @Override
            public Documentinfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                Documentinfo documentinfo=new Documentinfo();
                documentinfo.setId(rs.getString("id"));
                documentinfo.setDocument(rs.getString("document"));
                documentinfo.setFile_name(rs.getString("file_name"));
                return documentinfo;
            }
        }, doc);
        System.out.println(documentresult);

    }
    public String selectDocumentID(int file_id){
        String sql="select id from SPRING_AI_DOCUMENT where file_id = ?";
        String id=jdbcTemplate.queryForObject(sql,String.class,file_id);
        System.out.println(id);
        return id;

    }

    public void delatedocument(String id){
        //删除document表
        String sql="delete from SPRING_AI_DOCUMENT where id = ?";
        jdbcTemplate.update(sql,id);



    }
    public void updatedocument(String doc,String id){

        //修改某一id文本内容
        String sql = "update SPRING_AI_DOCUMENT set Document=? where file_id = ?";
        int rows=jdbcTemplate.update(sql,doc,id);
        System.out.println(rows);




    }

}
