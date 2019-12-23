package cn.iecas.datasets.image.pojo.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author vanishrain
 */
@Data
public class TileRequestDTO {
    private Integer pageNo = 1;

    private Integer pageSize = 10;

    private Integer imageDatasetId;


}
