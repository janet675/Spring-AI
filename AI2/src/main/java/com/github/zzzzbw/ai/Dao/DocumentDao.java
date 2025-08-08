package com.github.zzzzbw.ai.Dao;
import com.github.zzzzbw.ai.entity.Documentinfo;
import com.github.zzzzbw.ai.entity.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
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
        jdbcTemplate.queryForObject(sql,String.class,String.class,doc);

    }
    public String selectDocumentID(int file_id){
        String sql="select id from SPRING_AI_DOCUMENT where file_id = ?";
        String id=jdbcTemplate.queryForObject(sql,String.class,file_id);
        return id;

    }

    public void delatedocument(String id){
        //删除document表
        jdbcTemplate.update("delete from SPRING_AI_DOCUMENT where id = ?",id);



    }
    public void updatedocument(String doc,String id){

        //修改某一id文本内容
        String sql = "update SPRING_AI_DOCUMENT set Document=? where file_id = ?";
        jdbcTemplate.update(sql,doc,id);



    }

}
