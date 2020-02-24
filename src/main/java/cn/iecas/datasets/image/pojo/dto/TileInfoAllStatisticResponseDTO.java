package cn.iecas.datasets.image.pojo.dto;


import cn.iecas.datasets.image.pojo.entity.TileInfoStatistic;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TileInfoAllStatisticResponseDTO {
    private int count;
    private List<Map<String,Integer>> content;

}
