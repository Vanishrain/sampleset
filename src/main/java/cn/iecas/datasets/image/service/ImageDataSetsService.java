package cn.iecas.datasets.image.service;


import cn.iecas.datasets.image.pojo.domain.ImageDataSetInfoDO;
import cn.iecas.datasets.image.pojo.dto.*;
import cn.iecas.datasets.image.pojo.entity.Tile;

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
     * 根据样本集id获取样本集信息
     * @param imageDatasetId
     * @return
     */
    ImageDataSetInfoDO getImageDatasetInfoById(int imageDatasetId);

    /**
     * 获取影像数据集详细信息
     * @param imageDatasetInfoRequestDTO
     * @return
     */
    ImageDataSetInfoDTO listImageDatasetInfoDetail(ImageDataSetInfoRequestDTO imageDatasetInfoRequestDTO);

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
    void deleteImageDataSetByIds(int[] datasetIds) throws Exception;
}
