package cn.iecas.sampleset.pojo.dto.response;

import cn.iecas.sampleset.pojo.enums.TransferStatus;
import lombok.Data;

@Data
public class SampleTransferStatus {
    /**
     * md5值
     */
    private String md5;

    /**
     * 总分片数量
     */
    private int chunks;


    /**
     * 样本集id
     */
    private int sampleSetId;

    /**
     * 文件当前的分块数
     */
    private int uploadedChunk;

    /**
     * 上传状态
     */
    private TransferStatus transferStatus;
}
