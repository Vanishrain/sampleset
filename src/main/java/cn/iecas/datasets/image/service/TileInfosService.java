package cn.iecas.datasets.image.service;


import cn.iecas.datasets.image.pojo.domain.TileInfosDO;
import cn.iecas.datasets.image.pojo.dto.*;
import cn.iecas.datasets.image.pojo.entity.Tile;


import java.util.List;

public interface TileInfosService {
    void insertTileInfo(TileInfosDO tileInfosDO);
    TileSetDTO listTilesByDataSetId(TileRequestDTO tileRequestDTO);
    Tile getTileByName(int dataSetId, String tileName, String type);
    TileInfoAllStatisticResponseDTO getStatistic(TileInfoStatParamsDTO tileInfoStatParamsDTO);
    TileInfoStatisticResponseDTO getStatisticByIds(TileInfoStatParamsDTO tileInfoStatParamsDTO);

}
