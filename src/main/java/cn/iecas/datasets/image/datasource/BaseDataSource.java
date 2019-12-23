package cn.iecas.datasets.image.datasource;

import cn.iecas.datasets.image.pojo.dto.TileSetDTO;
import cn.iecas.datasets.image.pojo.entity.Tile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Component
public interface BaseDataSource {

    /**
     * 关闭数据源连接
     */
    void close();

    /**
     * 初始化数据源连接
     */
    void initialize();

    /**
     * 根据数据集id删除全部切片数据
     * @param imageSetId
     */
    void deleteImageSetById(int imageSetId);

    /**
     * 根据切片名称获取数据集中的切片
     * @param imageName
     * @return
     */
    Tile getImageByName( String imageName);

    /**
     * 根据切片名称删除数据集中的切片
     * @param imageSetId
     * @param imageName
     */
    void deleteImageByName(int imageSetId, String imageName);


    /**
     * 分页获取数据集切片
     * @param imagePathList
     * @return
     */
    TileSetDTO getImages(List<String> imagePathList) throws Exception;

}
