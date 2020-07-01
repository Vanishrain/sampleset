package cn.iecas.sampleset.pojo.dto;

import cn.iecas.sampleset.pojo.entity.DatasetTileInfoStatistic;
import lombok.Data;

import java.util.List;

@Data
public class TileInfoStatisticResponseDTO {
    private List<DatasetTileInfoStatistic> content;
    private int count=0;
}
