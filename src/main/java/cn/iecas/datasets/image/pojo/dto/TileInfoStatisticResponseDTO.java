package cn.iecas.datasets.image.pojo.dto;

import cn.iecas.datasets.image.pojo.entity.DatasetTileInfoStatistic;
import lombok.Data;

import java.util.List;

@Data
public class TileInfoStatisticResponseDTO {
    private List<DatasetTileInfoStatistic> content;
    private int count=0;
}
