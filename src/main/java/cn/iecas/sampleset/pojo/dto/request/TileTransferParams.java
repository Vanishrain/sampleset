package cn.iecas.sampleset.pojo.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Data
public class TileTransferParams {
    //当前为第几块分片
    private int chunk;

    //总分片数量
    private int chunks;

    // MD5
    private String md5;

    //文件名
    private String name;

    //样本集id
    private int imagesetid;

    //创建时间
    private Date create_time;

    //分片对象
    private MultipartFile file;

}
