package cn.iecas.datasets.image.service;

import cn.iecas.datasets.image.pojo.domain.TileTransferInfoDO;
import cn.iecas.datasets.image.pojo.dto.request.TileTransferParamsDTO;
import cn.iecas.datasets.image.pojo.dto.response.TileTransferStatusDTO;
import cn.iecas.datasets.image.pojo.entity.uploadFile.TransferStatus;

import java.io.IOException;

/**
 * 存储操作的service
 */
public interface TransferService {
    void deleteFile(String md5);
    void download(int imagesetid) throws Exception;
    void setTransferStatus(int imageDatasetId, String md5, TransferStatus transferStatus);
    TileTransferInfoDO getTileTransferInfoByImageDatasetIdAndMD5(int imageDataId, String md5);
    TileTransferStatusDTO checkFileMd5(int imageDatasetId, String md5) throws Exception;
    String transferTiles(TileTransferParamsDTO tileTransferParamsDTO, String uploadFilePath) throws Exception;
    boolean checkAndSetUploadProgress(TileTransferParamsDTO tileTransferParamsDTO, String uploadFilePath) throws IOException;

}
