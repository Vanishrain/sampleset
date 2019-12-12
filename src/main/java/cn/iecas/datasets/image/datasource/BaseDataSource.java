package cn.iecas.datasets.image.datasource;

import cn.iecas.datasets.image.pojo.dto.ImageSetDTO;
import cn.iecas.datasets.image.pojo.entity.Image;

public interface BaseDataSource {
    void close();

    void initialize();

    ImageSetDTO getImages(String path);

    long getSizeByDirectory(String path);

    Image getImageByName(String path, String imageName);

    ImageSetDTO getImages(String path, int pageNo, int pageSize);

    long getCountByDirectory(String path, String... postfix);

}
