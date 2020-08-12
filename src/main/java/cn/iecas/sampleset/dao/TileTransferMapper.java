package cn.iecas.sampleset.dao;

import cn.iecas.sampleset.pojo.domain.SampleSetTransferInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

@Repository
public interface TileTransferMapper extends BaseMapper<SampleSetTransferInfo> {
    SampleSetTransferInfo getByDatasetIdAndMD5(int datasetId, String md5);
    void addChunkCount(int sampleSetId, String md5, int chunks, int count);

}
