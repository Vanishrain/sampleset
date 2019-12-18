package cn.iecas.datasets.image.service.impl;

import cn.iecas.datasets.image.dao.ImageDatasetMapper;
import cn.iecas.datasets.image.dao.TileInfosMapper;
import cn.iecas.datasets.image.pojo.domain.TileInfosDO;
import cn.iecas.datasets.image.pojo.dto.*;
import cn.iecas.datasets.image.pojo.entity.DatasetTileInfoStatistic;
import cn.iecas.datasets.image.pojo.entity.Tile;
import cn.iecas.datasets.image.pojo.entity.TileInfoStatistic;
import cn.iecas.datasets.image.service.TileInfosService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class TileInfosServiceImpl extends ServiceImpl<TileInfosMapper, TileInfosDO> implements TileInfosService {


    @Autowired
    ImageDatasetMapper imageDatasetMapper;
    @Override
    public void insertTileInfo(TileInfosDO tileInfoDO) {
        this.baseMapper.insertTilesInfo(tileInfoDO);
        if(tileInfoDO.getImagesetid()!=null) {
            imageDatasetMapper.updateImageDataset(1,tileInfoDO.getImagesetid(),"number");
            if (tileInfoDO.getTargetNum() != null ) {
                imageDatasetMapper.updateImageDataset(tileInfoDO.getTargetNum(),tileInfoDO.getImagesetid(),"targetnum");
            }
        }
        log.info("成功插入切片数据imagesetid:{}",tileInfoDO.getImagesetid());
    }


    @Override
    public TileSetDTO listTilesByDataSetId(TileRequestDTO tileRequestDTO) {
        return null;
    }

    @Override
    public Tile getTileByName(int dataSetId, String tileName, String type) {
        return null;
    }


    /**
     * 根据数据集id返回返回属于该id的切片的年月日数据增长信息，当不指定imagesetidPre是则默认返回全部。
     * @param tileInfoStatParamsDTO
     * @return
     */
    @Override
    public  TileInfoAllStatisticResponseDTO getStatistic(TileInfoStatParamsDTO tileInfoStatParamsDTO) {
       List<TileInfoStatistic>  tileInfoStatisticList=this.baseMapper.getStatistic(tileInfoStatParamsDTO);
       int count=0;
       for(TileInfoStatistic tileInfoStatistic:tileInfoStatisticList){
           count=count+tileInfoStatistic.getCount();
           tileInfoStatistic.setCount(count);
       }
       TileInfoAllStatisticResponseDTO tileInfoAllStatisticResponseDTO=new TileInfoAllStatisticResponseDTO();
       tileInfoAllStatisticResponseDTO.setContent(tileInfoStatisticList);
       tileInfoAllStatisticResponseDTO.setCount(tileInfoStatisticList.size());
       return tileInfoAllStatisticResponseDTO;
    }

    /**
     * 根据数据集id集合，返回属于该id集合的切片的年月日数据增长信息
     * @param tileInfoStatParamsDTO
     * @return
     */
    @Override
    public TileInfoStatisticResponseDTO getStatisticByIds(TileInfoStatParamsDTO tileInfoStatParamsDTO) {
        TileInfoStatParamsDTO tileInfoStatParamsDTOPre=new TileInfoStatParamsDTO();
        tileInfoStatParamsDTOPre.setBeginTime(tileInfoStatParamsDTO.getBeginTime().getTime());
        tileInfoStatParamsDTOPre.setEndTime(tileInfoStatParamsDTO.getEndTime().getTime());
        tileInfoStatParamsDTOPre.setTimeStep(tileInfoStatParamsDTO.getTimeStep());
        Integer count;
        TileInfoStatisticResponseDTO tileInfoStatisticResponseDTO=new TileInfoStatisticResponseDTO();
        List<DatasetTileInfoStatistic> datasetTileInfoStatistics = new ArrayList<>();
        for(String tempDatasetid:tileInfoStatParamsDTO.getImagesetid()) {
            DatasetTileInfoStatistic datasetTileInfoStatistic=new DatasetTileInfoStatistic();
            datasetTileInfoStatistic.setImagesetid(Integer.valueOf(tempDatasetid));
            count=0;
            tileInfoStatParamsDTOPre.setImagesetidPre(Integer.valueOf(tempDatasetid));
            List<TileInfoStatistic> tileInfoStatisticList = this.baseMapper.getStatistic(tileInfoStatParamsDTOPre);
            for (TileInfoStatistic tileInfoStatistic : tileInfoStatisticList) {
                count = count + tileInfoStatistic.getCount();
                tileInfoStatistic.setCount(count);
            }
            datasetTileInfoStatistic.setContent(tileInfoStatisticList);
            datasetTileInfoStatistics.add(datasetTileInfoStatistic);
        }
        tileInfoStatisticResponseDTO.setContent(datasetTileInfoStatistics);
        tileInfoStatisticResponseDTO.setCount(datasetTileInfoStatistics.size());
        return tileInfoStatisticResponseDTO;
    }
}
