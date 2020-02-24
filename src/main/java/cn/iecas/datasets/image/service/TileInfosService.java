package cn.iecas.datasets.image.service;

import cn.iecas.datasets.image.pojo.domain.TileInfosDO;
import cn.iecas.datasets.image.pojo.dto.*;
import cn.iecas.datasets.image.pojo.dto.request.TileTransferParamsDTO;
import cn.iecas.datasets.image.pojo.entity.Tile;

import java.util.List;
import java.util.Map;

public interface TileInfosService {
    void deleteImages(int[] tileIds) throws Exception;
    Tile getTileByType(int tileId, String type) throws Exception;
    void deleteByImageDatasetId(int imgdatasetid) throws Exception;
    List<TileInfosDO> getTileInfoNotInNameList(List<String> nameList);
    void uploadTiles(TileTransferParamsDTO tileTransferParamsDTO) throws Exception;
    TileSetDTO listTilesByDataSetId(TileRequestDTO tileRequestDTO) throws Exception;
    TileInfoAllStatisticResponseDTO getStatistic(TileInfoStatParamsDTO tileInfoStatParamsDTO);
    TileInfoStatisticResponseDTO getStatisticByIds(TileInfoStatParamsDTO tileInfoStatParamsDTO);
}
