package cn.iecas.datasets.image.service.impl;

import cn.iecas.datasets.image.dao.ImageDatasetMapper;
import cn.iecas.datasets.image.dao.TileInfosMapper;
import cn.iecas.datasets.image.datasource.BaseDataSource;
import cn.iecas.datasets.image.pojo.domain.TileInfosDO;
import cn.iecas.datasets.image.pojo.dto.*;
import cn.iecas.datasets.image.pojo.domain.ImageDataSetInfoDO;
import cn.iecas.datasets.image.pojo.entity.Tile;
import cn.iecas.datasets.image.pojo.entity.Statistic;
import cn.iecas.datasets.image.service.ImageDataSetsService;
import cn.iecas.datasets.image.service.TileInfosService;
import cn.iecas.datasets.image.utils.FastDFSUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class ImageDataSetsServiceImpl extends ServiceImpl<ImageDatasetMapper, ImageDataSetInfoDO> implements ImageDataSetsService {
    @Autowired
    BaseDataSource baseDataSource;

    @Autowired
    TileInfosService tileInfosService;

    /**
     * 获取样本集的统计信息
     * @return
     */
    @Override
    public ImageDataSetStatisticDTO getStatistic() {
        long totalTargetNum = 0;
        ImageDataSetStatisticDTO imageDataSetStatisticDTO = new ImageDataSetStatisticDTO();
        List<Statistic> statisticList = this.baseMapper.getStatistic();
        for (Statistic statistic : statisticList)
            totalTargetNum += statistic.getTargetNum();

        imageDataSetStatisticDTO.setTargetNum(totalTargetNum);
        imageDataSetStatisticDTO.setDataSetNum(statisticList.size());
        return imageDataSetStatisticDTO;
    }

    /**
     * 根据id更新样本集信息
     * @param imageDataSetInfoDO
     */
    @Override
    public void updateImageDataSetInfoById(ImageDataSetInfoDO imageDataSetInfoDO) {
        this.baseMapper.updateById(imageDataSetInfoDO);
    }


    /**
     * 分页获取样本集信息
     * @param imageDatasetInfoRequestDTO
     * @return
     */
    @Override
    public ImageDataSetInfoDTO listImageDatasetInfoDetail(ImageDataSetInfoRequestDTO imageDatasetInfoRequestDTO) {
        ImageDataSetInfoDTO imageDataSetInfoDTO = new ImageDataSetInfoDTO();
        int pageNo = imageDatasetInfoRequestDTO.getPageNo() !=0 ? imageDatasetInfoRequestDTO.getPageNo() : 1;
        int pageSize = imageDatasetInfoRequestDTO.getPageSize() !=0 ? imageDatasetInfoRequestDTO.getPageSize() : 10;

        Page<ImageDataSetInfoDO> page = new Page<>();
        page.setCurrent(pageNo);
        page.setSize(pageSize);

        IPage imageDataSetInfoDOIPage = this.baseMapper.listImageDataSetInfos(page, imageDatasetInfoRequestDTO);
        imageDataSetInfoDTO.setTotal(imageDataSetInfoDOIPage.getTotal());
        imageDataSetInfoDTO.setList(imageDataSetInfoDOIPage.getRecords());
        return imageDataSetInfoDTO;
    }

    /**
     * 根据样本集id获取样本集信息
     * @param imageDatasetId
     * @return
     */
    public ImageDataSetInfoDO getImageDatasetInfoById(int imageDatasetId){
        return this.baseMapper.selectById(imageDatasetId);
    }


    /**
     * 删除image_info中的数据集信息，同时删除tile_info表中对应数据集id的切片数据
     * @param datasetIds
     */
    @Override
    public void deleteImageDataSetByIds(int[] datasetIds) throws Exception {
        for(int datasetId:datasetIds)
            deleteImageDataSetById(datasetId);
    }


    /**
     * 根据id删除数据集信息和数据
     * @param imageDataSetId
     */
    @Override
    public void deleteImageDataSetById(int imageDataSetId) throws Exception {
        log.info("删除id为：{}的样本集",imageDataSetId);
        this.baseMapper.deleteById(imageDataSetId);
        tileInfosService.deleteByImageDatasetId(imageDataSetId);
        log.info("删除样本集{}成功",imageDataSetId);
    }

    /**
     * 增加一条影像数据集信息
     * @param imageDataSetInfoDO
     */
    @Override
    public void insertImageDataSet(ImageDataSetInfoDO imageDataSetInfoDO){
        this.baseMapper.insertDataset(imageDataSetInfoDO);
        log.info("增加一条影像数据集信息");
    }

}
