package cn.iecas.datasets.image.pojo.dto;

import cn.iecas.datasets.image.pojo.entity.Tile;
import lombok.Data;

import java.util.List;

/**
 * @author vanishrain
 */
@Data
public class TileSetDTO {

//    private Integer pageNo;
    private Integer totalCount;
    private List<Tile> tileList;

}
