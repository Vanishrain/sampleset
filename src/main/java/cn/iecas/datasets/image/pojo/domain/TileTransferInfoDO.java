package cn.iecas.datasets.image.pojo.domain;

import cn.iecas.datasets.image.pojo.entity.uploadFile.TransferStatus;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "tile_upload_info")
public class TileTransferInfoDO {

    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    //已上传的分片数
    @TableField(value="chunk")
    private int chunk;

    //总分片数量
    @TableField(value="chunks")
    private int chunks;

    // MD5
    @TableField(value="md5")
    private String md5;

    //属于该样本集的第几次上传
    @TableField(value="version")
    private int version;

    //文件名
    @TableField(value="file_name")
    private String fileName;

    //样本集id
    @TableField(value="dataset_id")
    private int imageDatasetId;

    //创建时间
    private Date createTime;

    //上传状态
    @TableField(value="status")
    private TransferStatus transferStatus;



    @Override
    public String toString() {
        return "MultipartFileParam{" +
                ", chunks=" + chunks +
                ", chunk=" + chunk +
                ", name='" + fileName + '\'' +
                ", md5='" + md5 + '\'' +
                ", imagesetid=" + imageDatasetId +
                '}';
    }
}
