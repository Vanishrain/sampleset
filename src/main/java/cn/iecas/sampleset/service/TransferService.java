package cn.iecas.sampleset.service;

import cn.iecas.sampleset.pojo.domain.SampleSetTransferInfo;
import cn.iecas.sampleset.pojo.dto.request.SampleSetTransferParams;
import cn.iecas.sampleset.pojo.dto.response.SampleTransferStatus;
import cn.iecas.sampleset.pojo.enums.TransferStatus;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;

/**
 * 存储操作的service
 */
public interface TransferService extends IService<SampleSetTransferInfo> {
    void setTransferStatus(int sampleSetId, String md5, TransferStatus transferStatus);
    SampleSetTransferInfo getSampleTransferInfoBySampleSetIdAndMD5(int sampleSetId, String md5);
    SampleTransferStatus checkFileMd5(int sampleSetId, String md5) throws Exception;
    String transferTiles(SampleSetTransferParams sampleSetTransferParams, String uploadFilePath) throws Exception;
    boolean checkAndSetUploadProgress(SampleSetTransferParams sampleSetTransferParams, String uploadFilePath) throws IOException;

}
