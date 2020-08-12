package cn.iecas.sampleset.pojo.domain;

import cn.iecas.sampleset.pojo.enums.TransferStatus;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sample_tile_upload_info")
public class SampleSetTransferInfo {

    @TableId(value = "id",type = IdType.AUTO)
    private int id;


    /**
     * 总分片数量
     */
    private int chunks;

    /**
     * MD5
     */
    private String md5;

    /**
     * 属于该样本集的第几次上传
     */
    private int version;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 样本集id
     */
    private int sampleSetId;


    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 已上传的分片数
     */
    private int uploadedChunk;

    /**
     * 上传状态
     */
    @TableField(value="status")
    private TransferStatus transferStatus;





    @Override
    public String toString() {
        return "MultipartFileParam{" +
                ", chunks=" + chunks +
                ", chunk=" + uploadedChunk +
                ", name='" + fileName + '\'' +
                ", md5='" + md5 + '\'' +
                ", imagesetid=" + sampleSetId +
                '}';
    }
}
