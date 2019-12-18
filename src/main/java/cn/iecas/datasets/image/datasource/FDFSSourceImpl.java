package cn.iecas.datasets.image.datasource;

import cn.iecas.datasets.image.pojo.dto.TileSetDTO;
import cn.iecas.datasets.image.pojo.entity.Tile;

public class FDFSSourceImpl implements BaseDataSource {

    /**
     * 关闭数据源连接
     */
    @Override
    public void close() {

    }


    /**
     * 初始化数据源连接
     */
    @Override
    public void initialize() {

    }


    /**
     * 根据数据集id删除全部切片数据
     * @param imageSetId
     */
    @Override
    public void deleteImageSetById(int imageSetId) {

    }


    /**
     * 根据切片名称获取数据集中的切片
     * @param imageSetId
     * @param imageName
     * @return
     */
    @Override
    public Tile getImageByName(int imageSetId, String imageName) {
        return null;
    }

    /**
     * 根据切片名称删除数据集中的切片
     * @param imageSetId
     * @param imageName
     */
    @Override
    public void deleteImageByName(int imageSetId, String imageName) {

    }

    /**
     * 分页获取数据集切片
     * @param imageSetId
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Override
    public TileSetDTO getImages(int imageSetId, int pageNo, int pageSize) {
        return null;
    }

}
