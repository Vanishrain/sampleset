package cn.iecas.datasets.image.service.impl;

import cn.iecas.datasets.image.dao.ImageDatasetMapper;
import cn.iecas.datasets.image.dao.TileInfosMapper;
import cn.iecas.datasets.image.datasource.BaseDataSource;
import cn.iecas.datasets.image.pojo.dto.ImageDataSetInfoRequestDTO;
import cn.iecas.datasets.image.pojo.dto.TileSetDTO;
import cn.iecas.datasets.image.pojo.dto.TileRequestDTO;
import cn.iecas.datasets.image.pojo.domain.ImageDataSetInfoDO;
import cn.iecas.datasets.image.pojo.dto.ImageDataSetStatisticDTO;
import cn.iecas.datasets.image.pojo.entity.Tile;
import cn.iecas.datasets.image.pojo.entity.Statistic;
import cn.iecas.datasets.image.service.ImageDataSetsService;
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

    @Value("${datasource.file.rootPath}")
    private String rootPath;

    @Autowired
    TileInfosMapper tileInfosMapper;

    @Autowired
    BaseDataSource baseDataSource;

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

    @Override
    public void updateImageDataSetInfoById(ImageDataSetInfoDO imageDataSetInfoDO) {
        this.baseMapper.updateById(imageDataSetInfoDO);
    }

    @Override
    public Tile getImageByName(int imageDataSetId, String imageName, String type) {
        return baseDataSource.getImageByName(imageName);
    }

    /**
     * 根据数据集id获取相应的切片数据
     * @param tileRequestDTO 获取影像请求参数
     * @return
     */
    @Override
    public TileSetDTO listImagesByDataSetId(TileRequestDTO tileRequestDTO) {
        int imageDatasetId = tileRequestDTO.getImageDatasetId();
        ImageDataSetInfoDO imageDataSetInfoDO = getImageDatasetInfoById(imageDatasetId);

        if (imageDataSetInfoDO == null){
            log.info("影像数据集id：{} 不存在",imageDatasetId);
            return new TileSetDTO();
        }

        List<String> imagePathList = (List<String>) tileInfosMapper.getAll(new Page(tileRequestDTO.getPageNo(), tileRequestDTO.getPageSize()), tileRequestDTO.getImageDatasetId());
        TileSetDTO tileSetDTO = null;
        try {
            tileSetDTO = baseDataSource.getImages(imagePathList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        tileSetDTO.setTotalCount(imageDataSetInfoDO.getNumber());
        return tileSetDTO;
    }


    /**
     * 分页获取样本集信息
     * @param imageDatasetInfoRequestDTO
     * @return
     */
    @Override
    public List<ImageDataSetInfoDO> listImageDatasetInfoDetail(ImageDataSetInfoRequestDTO imageDatasetInfoRequestDTO) {
        int pageNo = imageDatasetInfoRequestDTO.getPageNo() !=0 ? imageDatasetInfoRequestDTO.getPageNo() : 1;
        int pageSize = imageDatasetInfoRequestDTO.getPageSize() !=0 ? imageDatasetInfoRequestDTO.getPageSize() : 10;
        Page<ImageDataSetInfoDO> page = new Page<>();
        page.setCurrent(pageNo);
        page.setSize(pageSize);
        List<ImageDataSetInfoDO> imageDataSetInfoDOList = this.baseMapper.listImageDataSetInfos(page, imageDatasetInfoRequestDTO);
        return imageDataSetInfoDOList;
    }

    public ImageDataSetInfoDO getImageDatasetInfoById(int imageDatasetId){
        return this.baseMapper.selectById(imageDatasetId);
    }


    /**
     * 删除image_info中的数据集信息，同时删除tile_info表中对应数据集id的切片数据
     * @param datasetIds
     */
    @Override
    public  void  deleteImageDataSetByIds(List<String> datasetIds) {
        for(String temp:datasetIds) {
            deleteImageDataSetById(Integer.valueOf(temp));
        }
    }


    /**
     * 根据id删除数据集信息和数据
     * @param imageDataSetId
     */
    @Override
    public void deleteImageDataSetById(int imageDataSetId) {
        ImageDataSetInfoDO imageDataSetInfoDO = this.baseMapper.selectById(imageDataSetId);
        int flag = this.baseMapper.deleteById(imageDataSetId);
        if(flag!=0){
            tileInfosMapper.deleteByImagesetid(imageDataSetId);
            log.info("成功删除数据集:{}和数据集下的切片信息",imageDataSetId);
        }else{
            log.info("id：{} 数据集不存在", imageDataSetId);
        }
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
