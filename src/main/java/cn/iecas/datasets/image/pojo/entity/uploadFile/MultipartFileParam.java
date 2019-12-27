package cn.iecas.datasets.image.pojo.entity.uploadFile;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class MultipartFileParam {

    //总分片数量
    private int chunks;
    //当前为第几块分片
    private int chunk;
    //当前分片大小
    private long size = 0L;
    //文件名
    private String name;
    //分片对象
    private MultipartFile file;
    // MD5
    private String md5;
    private int imagesetid; //数据集id

    @Override
    public String toString() {
        return "MultipartFileParam{" +
                ", chunks=" + chunks +
                ", chunk=" + chunk +
                ", size=" + size +
                ", name='" + name + '\'' +
                ", file=" + file +
                ", md5='" + md5 + '\'' +
                ", imagesetid=" + imagesetid +
                '}';
    }
}
