package cn.iecas.datasets.image.dao;

import cn.iecas.datasets.image.pojo.domain.TileInfosDO;
import cn.iecas.datasets.image.pojo.dto.TileInfoStatParamsDTO;
import cn.iecas.datasets.image.pojo.dto.TileRequestDTO;
import cn.iecas.datasets.image.pojo.entity.TileInfoStatistic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TileInfosMapper extends BaseMapper<TileInfosDO> {
    List<TileInfosDO> getIdByPath(@Param("imagePathList") List<String> imagePathList);
    void deleteByImagesetid(Integer imagesetid);
    void insertTilesInfo(TileInfosDO tileInfoDO);
    String getVisualPath(int tileId);
    String getStoragePath(String visualPath);
    int getImageDataSetId(Integer tileIds);
    List<TileInfosDO> getAllTileById(Integer imagesetid);
    TileInfosDO getTileByName(int tileId);
    IPage<TileInfosDO> listTilesByDataSetId(Page page, @Param("tileRequestDTO") TileRequestDTO tileRequestDTO);
    List<TileInfoStatistic> getStatistic(TileInfoStatParamsDTO tileInfoStatParamsDTO);
    List<TileInfoStatistic> getStatisticByDataSets(TileInfoStatParamsDTO tileInfoStatParamsDTO);

}
