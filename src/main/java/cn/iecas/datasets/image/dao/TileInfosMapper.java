package cn.iecas.datasets.image.dao;

import cn.iecas.datasets.image.pojo.domain.TileInfosDO;
import cn.iecas.datasets.image.pojo.dto.TileInfoStatParamsDTO;
import cn.iecas.datasets.image.pojo.entity.TileInfoStatistic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TileInfosMapper extends BaseMapper<TileInfosDO> {
    String getNameByPath(@Param("visualPath") String imagePath);
    void deleteByImagesetid(Integer imagesetid);
    void insertTilesInfo(TileInfosDO tileInfoDO);
    String getVisualPath(int tileId);
    String getStoragePath(String visualPath);
//    TileInfosDO getTileByName(String imageName);
    TileInfosDO getTileByName(int tileId);
    IPage<String> getAll(Page page, int imageDatasetId);
    List<TileInfoStatistic> getStatistic(TileInfoStatParamsDTO tileInfoStatParamsDTO);

}
