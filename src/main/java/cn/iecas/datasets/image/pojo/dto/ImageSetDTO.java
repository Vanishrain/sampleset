package cn.iecas.datasets.image.pojo.dto;

import cn.iecas.datasets.image.pojo.entity.Image;
import lombok.Data;

import java.util.List;

/**
 * @author vanishrain
 */
@Data
public class ImageSetDTO {

    private Integer pageNo;
    private Integer totalCount;
    private List<Image> imageList;


}
