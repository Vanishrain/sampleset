package cn.iecas.sampleset.service;

import cn.iecas.sampleset.pojo.domain.SampleTransferInfo;
import cn.iecas.sampleset.pojo.dto.request.TileTransferParams;
import cn.iecas.sampleset.pojo.dto.response.SampleTransferStatus;
import cn.iecas.sampleset.pojo.enums.TransferStatus;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;

/**
 * 存储操作的service
 */
public interface TransferService extends IService<SampleTransferInfo> {
    void deleteFile(String md5);
    void download(int sampleSetId) throws Exception;
    void setTransferStatus(int sampleSetId, String md5, TransferStatus transferStatus);
    SampleTransferInfo getSampleTransferInfoBySampleSetIdAndMD5(int sampleSetId, String md5);
    SampleTransferStatus checkFileMd5(int sampleSetId, String md5) throws Exception;
    String transferTiles(TileTransferParams tileTransferParams, String uploadFilePath) throws Exception;
    boolean checkAndSetUploadProgress(TileTransferParams tileTransferParams, String uploadFilePath) throws IOException;

}
