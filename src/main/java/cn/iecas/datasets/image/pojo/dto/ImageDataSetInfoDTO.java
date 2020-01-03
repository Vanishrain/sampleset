package cn.iecas.datasets.image.pojo.dto;

import cn.iecas.datasets.image.pojo.domain.ImageDataSetInfoDO;
import lombok.Data;

import java.util.List;

@Data
public class ImageDataSetInfoDTO {
    private long total;
    private List<ImageDataSetInfoDO> list;
}
