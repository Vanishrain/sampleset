package cn.iecas.datasets.image.service;

import cn.iecas.datasets.image.pojo.domain.TileInfosDO;
import cn.iecas.datasets.image.pojo.dto.*;
import cn.iecas.datasets.image.pojo.entity.Tile;

public interface TileInfosService {
    void insertTileInfo(TileInfosDO tileInfosDO);
    TileSetDTO listTilesByDataSetId(TileRequestDTO tileRequestDTO) throws Exception;
    Tile getTileByName(String tileId, String type) throws Exception;
    TileInfoAllStatisticResponseDTO getStatistic(TileInfoStatParamsDTO tileInfoStatParamsDTO);
    TileInfoStatisticResponseDTO getStatisticByIds(TileInfoStatParamsDTO tileInfoStatParamsDTO);
    void deleteImages(Integer[] tileIds) throws Exception;
    String deleteByImageDatasetId(int imgdatasetid) throws Exception;
}
