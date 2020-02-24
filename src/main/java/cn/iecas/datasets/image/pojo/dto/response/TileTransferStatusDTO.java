package cn.iecas.datasets.image.pojo.dto.response;

import cn.iecas.datasets.image.pojo.entity.uploadFile.TransferStatus;
import lombok.Data;

@Data
public class TileTransferStatusDTO {
    //当前为第几块分片
    private int chunk;

    //md5
    private String md5;

    //总分片数量
    private int chunks;

    //样本集id
    private int imagesetid;

    //上传状态
    private TransferStatus transferStatus;
}
