package com.github.zzzzbw.ai.Dao;

import com.github.zzzzbw.ai.entity.FileDocumentInfo;
import com.github.zzzzbw.ai.entity.FileInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;


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
        jdbcTemplate.update(sql, args);

    }

    public int selectid(String fileName) {
        String sql = "select id from SPRING_AI_FILE  where file_name = ?";
        int id = jdbcTemplate.queryForObject(sql, int.class, fileName);
        System.out.println(id);
        return id;

    }

    public List<Integer> selectfile(String fileName) {
        String sql = "select id from SPRING_AI_FILE  where file_name = ?";

        List<Integer> list1 = jdbcTemplate.queryForList(sql, new Object[]{fileName}, Integer.class);

        return list1;


    }

    public List<String> selectdocumentinfo(String fileName) {
        String sql = "select Document from SPRING_AI_DOCUMENT where file_name = ?";
        List<String> list = jdbcTemplate.queryForList(sql, new Object[]{fileName}, String.class);
        return list;
    }

    public void delateFile(int id) {

        String sql = "update SPRING_AI_FILE set status=? where id = ?";

        int rows = jdbcTemplate.update(sql, 0, id);

        System.out.println(rows);


    }

    public void updateFileStatus(int status, int id) {
        String sql = "update SPRING_AI_FILE set status=? where id = ?";
        int rows = jdbcTemplate.update(sql, status, id);
        System.out.println(rows);

    }


    public List<FileDocumentInfo> selectFileDocumentInfoByCondition(String fileName, String content) {
        String sql = "select a.id as file_id, a.file_name, b.id as document_id, b.Document as document_content " +
                "from SPRING_AI_FILE a, SPRING_AI_DOCUMENT b " +
                " where a.id = b.file_id and a.status!=0";
        List<Object> args = new ArrayList<>();
        if (StringUtils.isNotBlank(fileName)) {
            sql += " and a.file_name like '%' ? '%'";
            args.add(fileName);
        }
        if (StringUtils.isNotBlank(content)) {
            sql += " and b.Document like '%' ? '%'";
            args.add(content);
        }
        String[] arr = new String[args.size()];

        args.toArray(arr);
        System.out.println("Array：");
        for (String item : arr) {
            System.out.print(item + ", ");
        }
        return jdbcTemplate.query(sql, arr, new BeanPropertyRowMapper<>(FileDocumentInfo.class));

    }
}
