package cn.iecas.datasets.image.datasource;

import cn.iecas.datasets.image.pojo.dto.ImageSetDTO;
import cn.iecas.datasets.image.pojo.entity.Image;

public class FDFSSourceImpl implements BaseDataSource {
    @Override
    public void close() {

    }

    @Override
    public void initialize() {

    }

    @Override
    public void deleteImageSetById(int imageSetId) {

    }


    @Override
    public Image getImageByName(int imageSetId, String imageName) {
        return null;
    }

    @Override
    public void deleteImageByName(int imageSetId, String imageName) {

    }

    @Override
    public ImageSetDTO getImages(int imageSetId, int pageNo, int pageSize) {
        return null;
    }

}
