package cn.iecas.sampleset.dao;

import cn.iecas.sampleset.pojo.domain.SampleInfo;
import cn.iecas.sampleset.pojo.dto.TileInfoStatParamsDTO;
import cn.iecas.sampleset.pojo.dto.SampleRequestParams;
import cn.iecas.sampleset.pojo.entity.TileInfoStatistic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SampleInfoMapper extends BaseMapper<SampleInfo> {
    void batchInsert(@Param("tileInfosDOList") List<SampleInfo> sampleInfoList);
    List<SampleInfo> getIdByPath(@Param("imagePathList") List<String> imagePathList);
    int getImageDataSetId(Integer tileIds);
    IPage<SampleInfo> listSampleInfos(Page page, @Param("sampleRequestParams") SampleRequestParams sampleRequestParams);
    List<TileInfoStatistic> getStatistic(TileInfoStatParamsDTO tileInfoStatParamsDTO);
    List<TileInfoStatistic> getStatisticByDataSets(TileInfoStatParamsDTO tileInfoStatParamsDTO);

}
