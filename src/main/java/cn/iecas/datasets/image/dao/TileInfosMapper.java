package cn.iecas.datasets.image.dao;


import cn.iecas.datasets.image.common.domain.QueryRequest;
import cn.iecas.datasets.image.pojo.domain.TileInfosDO;
import cn.iecas.datasets.image.pojo.dto.TileInfoStatParamsDTO;
import cn.iecas.datasets.image.pojo.entity.TileInfoStatistic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TileInfosMapper extends BaseMapper<TileInfosDO> {
    void deleteByImagesetid(Integer imagesetid);
    void insertTilesInfo(TileInfosDO tileInfoDO);
    String getStoragePath(TileInfosDO tileInfosDO);
    IPage<String> getAll(Page page, @Param("request") QueryRequest request);
    List<TileInfoStatistic> getStatistic(TileInfoStatParamsDTO tileInfoStatParamsDTO);

}
