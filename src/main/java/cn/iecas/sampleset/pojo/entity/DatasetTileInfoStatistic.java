package cn.iecas.sampleset.pojo.entity;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DatasetTileInfoStatistic {

    public DatasetTileInfoStatistic(int imagesetid, List<Map<String,Integer>> content){
        this.content = content;
        this.imagesetid = imagesetid;
    }

    private int imagesetid;
    private List<Map<String,Integer>> content;

}
