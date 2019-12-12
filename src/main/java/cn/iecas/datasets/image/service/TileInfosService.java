package cn.iecas.datasets.image.service;


import cn.iecas.datasets.image.pojo.domain.TileInfosDO;
import cn.iecas.datasets.image.pojo.dto.TileInfoAllStatisticResponseDTO;
import cn.iecas.datasets.image.pojo.dto.TileInfoStatParamsDTO;
import cn.iecas.datasets.image.pojo.dto.TileInfoStatisticResponseDTO;


import java.util.List;

public interface TileInfosService {
    void insertTileInfo(TileInfosDO tileInfosDO);
    void insertTileInfos(List<TileInfosDO> tileInfosDOS);
    TileInfoAllStatisticResponseDTO getStatistic(TileInfoStatParamsDTO tileInfoStatParamsDTO);
    TileInfoStatisticResponseDTO getStatisticByIds(TileInfoStatParamsDTO tileInfoStatParamsDTO);

}
