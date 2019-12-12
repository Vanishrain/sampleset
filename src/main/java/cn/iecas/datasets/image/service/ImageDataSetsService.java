package cn.iecas.datasets.image.service;


import cn.iecas.datasets.image.pojo.domain.ImageDataSetInfoDO;
import cn.iecas.datasets.image.pojo.dto.ImageDataSetInfoRequestDTO;
import cn.iecas.datasets.image.pojo.dto.ImageSetDTO;
import cn.iecas.datasets.image.pojo.dto.ImageRequestDTO;
import cn.iecas.datasets.image.pojo.dto.ImageDataSetStatisticDTO;
import cn.iecas.datasets.image.pojo.entity.Image;

import java.util.List;

/**
 * @author vanishrain
 */
public interface ImageDataSetsService {

    /**
     * 获取影像数据集的统计信息
     */
    ImageDataSetStatisticDTO getStatistic();

    /**
     * 根据id更新数据集信息
     * @param imageDataSetInfoDO
     */
    void updateImageDataSetInfoById(ImageDataSetInfoDO imageDataSetInfoDO);

    /**
     * 根据切片名称获取数据
     * @param imageDataSetId
     * @param imageName
     * @return
     */
    Image getImageByName(int imageDataSetId, String imageName, String type);

    /**
     * 根据影像数据集id获取分页影像
     * @param imageRequestDTO 获取影像请求参数
     * @return 影像数据信息
     */
    ImageSetDTO listImagesByDataSetId(ImageRequestDTO imageRequestDTO);

    /**
     * 获取影像数据集详细信息
     * @param imageDatasetInfoRequestDTO
     * @return
     */
    List<ImageDataSetInfoDO> listImageDatasetInfoDetail(ImageDataSetInfoRequestDTO imageDatasetInfoRequestDTO);

    /**
     * 根据id删除对应数据集，同时会删除数据
     * @param imageDataSetId
     */
    void deleteImageDataSetById(int imageDataSetId) throws Exception;


    /**
     * 增加数据集信息
      * @param imageDataSetInfoDO
     */
    void insertImageDataSet(ImageDataSetInfoDO imageDataSetInfoDO);

    /**
     * 删除image_info中的数据集信息，同时删除tile_info表中对应数据集id的切片数据
     * @param datasetIds
     */
    void deleteImageDataSetByIds(List<String> datasetIds) throws Exception;
}
