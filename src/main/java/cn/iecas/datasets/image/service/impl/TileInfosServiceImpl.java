package cn.iecas.datasets.image.service.impl;

import cn.iecas.datasets.image.dao.ImageDatasetMapper;
import cn.iecas.datasets.image.dao.TileInfosMapper;
import cn.iecas.datasets.image.datasource.BaseDataSource;
import cn.iecas.datasets.image.pojo.domain.ImageDataSetInfoDO;
import cn.iecas.datasets.image.pojo.domain.TileInfosDO;
import cn.iecas.datasets.image.pojo.dto.*;
import cn.iecas.datasets.image.pojo.entity.DatasetTileInfoStatistic;
import cn.iecas.datasets.image.pojo.entity.Tile;
import cn.iecas.datasets.image.pojo.entity.TileInfoStatistic;
import cn.iecas.datasets.image.service.TileInfosService;
import cn.iecas.datasets.image.utils.FastDFSUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class TileInfosServiceImpl extends ServiceImpl<TileInfosMapper, TileInfosDO> implements TileInfosService {
    @Value("${value.fastdfsServer}")
    private String fastdfsServer;   //FastDFS服务路径

    @Autowired
    ImageDatasetMapper imageDatasetMapper;
    @Autowired
    TileInfosMapper tileInfosMapper;
    @Autowired
    BaseDataSource baseDataSource;

    @Override
    public void deleteByImageDatasetId(int imagesetid) {
        List<TileInfosDO> tileInfosDOS = tileInfosMapper.getAllTileById(imagesetid);
        List<Integer> tileIds = new ArrayList<>();
        for (TileInfosDO tileInfosDO : tileInfosDOS){//得到所有切片id
            tileIds.add(tileInfosDO.getId());
        }

        for (int tileId : tileIds){
            baseDataSource.deletes(tileId);//删除切片数据
            //tileInfosMapper.deleteById(tileId);//删除切片库中信息
        }
    }

    /*
    * 根据切片id批量删除
    * */
    @Override
    public void deleteImages(Integer[] tileIds) {
        ImageDataSetInfoDO imageDataSetInfoDO;
        for (int tileId : tileIds){
            baseDataSource.deletes(tileId);//删除切片数据
            int imageDataSetId = tileInfosMapper.getImageDataSetId(tileId); //根据切片id得到数据集id
            tileInfosMapper.deleteById(tileId);//删除切片库中信息

            imageDataSetInfoDO = imageDatasetMapper.getImageDataSetById(imageDataSetId);
            imageDataSetInfoDO.setNumber(imageDataSetInfoDO.getNumber()-1);
            imageDatasetMapper.updateNumber(imageDataSetInfoDO);
        }
    }

    @Override
    public void insertTileInfo(TileInfosDO tileInfoDO) {
        this.baseMapper.insertTilesInfo(tileInfoDO);
        if (tileInfoDO.getImagesetid() != null) {
            imageDatasetMapper.updateImageDataset(1, tileInfoDO.getImagesetid(), "number");
            if (tileInfoDO.getTargetNum() != null) {
                imageDatasetMapper.updateImageDataset(tileInfoDO.getTargetNum(), tileInfoDO.getImagesetid(), "targetnum");
            }
        }
        log.info("成功插入切片数据imagesetid:{}", tileInfoDO.getImagesetid());
    }

    @Override
    public TileSetDTO listTilesByDataSetId(TileRequestDTO tileRequestDTO) {
        Page<String> page = new Page<>();
        page.setCurrent(tileRequestDTO.getPageNo());
        page.setSize(tileRequestDTO.getPageSize());
        List<String> imagePathList = tileInfosMapper.getAll(page, tileRequestDTO.getImageDatasetId()).getRecords();
        TileSetDTO tileSetDTO = null;
        try {
            tileSetDTO = baseDataSource.getImages(imagePathList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        tileSetDTO.setPageNo(1);
        tileSetDTO.setTotalCount(imagePathList.size());
        return tileSetDTO;
    }

    /**
     *
     * @param tileId
     * @param type
     * @return
     */
    @Override
    public Tile getTileByName(String tileId, String type) {
        Tile tile = new Tile();
        TileInfosDO tileInfosDO = tileInfosMapper.getTileByName(Integer.valueOf(tileId));
        tile.setName(tileInfosDO.getDataPath());
        if ("imgs".equals(type)) {
            tile.setBase64Tile(baseDataSource.getImageByPath(tileInfosDO.getStoragePath()));
        } else if ("visuals".equals(type)) {
            tile.setBase64Tile(baseDataSource.getImageByPath(tileInfosDO.getVisualPath()));
        } else if ("xmls".equals(type)) {
            tile.setBase64Tile(baseDataSource.getImageByPath(tileInfosDO.getLabelPath()));
        }

        return tile;
}


    /**
     * 根据数据集id返回
     * 返回属于该id的切片的年月日数据增长信息，当不指定imagesetidPre是则默认返回全部。
     * @param tileInfoStatParamsDTO
     * @return
     */
    @Override
    public  TileInfoAllStatisticResponseDTO getStatistic(TileInfoStatParamsDTO tileInfoStatParamsDTO) {
        int count=0;
        List<Map<String,Integer>> contentList = new ArrayList<>();
        List<TileInfoStatistic>  tileInfoStatisticList=this.baseMapper.getStatistic(tileInfoStatParamsDTO);
        for(TileInfoStatistic tileInfoStatistic:tileInfoStatisticList){
            Map<String,Integer> content = new HashMap<>();
            count=count+tileInfoStatistic.getCount();
            content.put(tileInfoStatistic.getStep(),count);
            contentList.add(content);
        }
        TileInfoAllStatisticResponseDTO tileInfoAllStatisticResponseDTO=new TileInfoAllStatisticResponseDTO();
        tileInfoAllStatisticResponseDTO.setContent(contentList);
        tileInfoAllStatisticResponseDTO.setCount(contentList.size());
        return tileInfoAllStatisticResponseDTO;
    }

    /**
     * 根据数据集id集合，返回属于该id集合的切片的年月日数据增长信息
     * @param tileInfoStatParamsDTO
     * @return
     */
    @Override
    public TileInfoStatisticResponseDTO getStatisticByIds(TileInfoStatParamsDTO tileInfoStatParamsDTO) {
        int accumulation = 0;
        int oldImageSetId = -1;
        TileInfoStatisticResponseDTO tileInfoStatisticResponseDTO=new TileInfoStatisticResponseDTO();
        List<TileInfoStatistic> tileInfoStatisticList = this.baseMapper.getStatisticByDataSets(tileInfoStatParamsDTO);
        Map<Integer,DatasetTileInfoStatistic> datasetTileInfoStatisticMap = new HashMap<>();

        for (TileInfoStatistic tileInfoStatistic : tileInfoStatisticList) {
            String step = tileInfoStatistic.getStep();
            DatasetTileInfoStatistic datasetTileInfoStatistic;
            int imageSetId = tileInfoStatistic.getImageSetId();

            if (datasetTileInfoStatisticMap.containsKey(imageSetId))
                datasetTileInfoStatistic = datasetTileInfoStatisticMap.get(imageSetId);
            else{

                datasetTileInfoStatistic = new DatasetTileInfoStatistic(imageSetId, new ArrayList<>());
                datasetTileInfoStatisticMap.put(imageSetId,datasetTileInfoStatistic);
            }


            List<Map<String,Integer>> contentList = datasetTileInfoStatistic.getContent() ;

            if (oldImageSetId != imageSetId){
                accumulation=0;
                oldImageSetId = imageSetId;
            }


            accumulation += tileInfoStatistic.getCount();

            Map<String,Integer> content = new HashMap<>();
            content.put(step,accumulation);
            contentList.add(content);
        }
        List<DatasetTileInfoStatistic> contentList = new ArrayList<>(datasetTileInfoStatisticMap.values());
        tileInfoStatisticResponseDTO.setContent(contentList);
        tileInfoStatisticResponseDTO.setCount(datasetTileInfoStatisticMap.size());
        return tileInfoStatisticResponseDTO;
    }
}
