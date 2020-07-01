package cn.iecas.sampleset.dao;

import cn.iecas.sampleset.pojo.domain.SampleTransferInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

@Repository
public interface TileTransferMapper extends BaseMapper<SampleTransferInfo> {
    SampleTransferInfo getByDatasetIdAndMD5(int datasetId, String md5);
    void addChunkCount(int imageDatasetId, String md5, int chunks, int count);

}
