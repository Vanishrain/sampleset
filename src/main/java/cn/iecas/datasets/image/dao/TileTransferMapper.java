package cn.iecas.datasets.image.dao;

import cn.iecas.datasets.image.pojo.domain.TileTransferInfoDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

@Repository
public interface TileTransferMapper extends BaseMapper<TileTransferInfoDO> {
    TileTransferInfoDO getByDatasetIdAndMD5(int datasetId, String md5);
    void addChunkCount(int imageDatasetId, String md5, int chunks, int count);

}
