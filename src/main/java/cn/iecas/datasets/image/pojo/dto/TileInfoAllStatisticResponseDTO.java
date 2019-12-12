package cn.iecas.datasets.image.pojo.dto;


import cn.iecas.datasets.image.pojo.entity.TileInfoStatistic;
import lombok.Data;

import java.util.List;

@Data
public class TileInfoAllStatisticResponseDTO {
    private List<TileInfoStatistic> content;
    private int count;

}
