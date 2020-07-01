package cn.iecas.sampleset.pojo.dto;


import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TileInfoAllStatisticResponseDTO {
    private int count;
    private List<Map<String,Integer>> content;

}
