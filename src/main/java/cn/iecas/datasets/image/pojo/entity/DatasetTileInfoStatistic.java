package cn.iecas.datasets.image.pojo.entity;

import lombok.Data;

import java.util.List;

@Data
public class DatasetTileInfoStatistic {
    private List<TileInfoStatistic> content;
    private int imagesetid;
}
