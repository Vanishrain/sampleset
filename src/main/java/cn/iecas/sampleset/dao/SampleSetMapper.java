package cn.iecas.sampleset.dao;

import cn.iecas.sampleset.pojo.domain.SampleSetInfo;
import cn.iecas.sampleset.pojo.dto.SampleSetInfoRequestParam;
import cn.iecas.sampleset.pojo.entity.Statistic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SampleSetMapper extends BaseMapper<SampleSetInfo> {
    List<Statistic> getStatistic();
    SampleSetInfo getImageDataSetById(int id);
    void updateCount(SampleSetInfo sampleSetInfo);
    void updateSampleSet(int value, int sampleSetid, String field);
    IPage<SampleSetInfo> listImageDataSetInfos(Page page, @Param("sampleSetInfoRequestParam") SampleSetInfoRequestParam sampleSetInfoRequestParam);
}
