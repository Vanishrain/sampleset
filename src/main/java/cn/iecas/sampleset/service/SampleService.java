package cn.iecas.sampleset.service;

import cn.iecas.sampleset.pojo.domain.SampleInfo;
import cn.iecas.sampleset.pojo.dto.TileInfoAllStatisticResponseDTO;
import cn.iecas.sampleset.pojo.dto.TileInfoStatParamsDTO;
import cn.iecas.sampleset.pojo.dto.TileInfoStatisticResponseDTO;
import cn.iecas.sampleset.pojo.dto.SampleRequestParams;
import cn.iecas.sampleset.pojo.dto.common.PageResult;
import cn.iecas.sampleset.pojo.dto.request.SampleSetTransferParams;
import cn.iecas.sampleset.pojo.entity.Sample;
import cn.iecas.sampleset.pojo.enums.SampleType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SampleService extends IService<SampleInfo> {
    void deleteSamples(List<Integer> tileIdList) throws Exception;
    void deleteBySampleSetId(int sampleSetId) throws Exception;
    Sample getSampleByTypeAndId(int tileId, SampleType sampleType) throws Exception;
    PageResult<Sample> listSamplesBySetId(SampleRequestParams sampleRequestParams) throws Exception;
    TileInfoAllStatisticResponseDTO getStatistic(TileInfoStatParamsDTO tileInfoStatParamsDTO);
    TileInfoStatisticResponseDTO getStatisticByIds(TileInfoStatParamsDTO tileInfoStatParamsDTO);
    SampleSetTransferParams uploadTiles(SampleSetTransferParams sampleSetTransferParams) throws Exception;

}
