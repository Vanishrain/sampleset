package cn.iecas.datasets.image.pojo.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author vanishrain
 */
@Data
public class TileRequestDTO {
    private Integer pageNo;

    private Integer pageSize;

    private Integer imageDatasetId;


}
