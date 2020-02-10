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
import com.baomidou.mybatisplus.core.metadata.IPage;
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
    public String deleteByImageDatasetId(int imagesetid) throws Exception {
        List<TileInfosDO> tileInfosDOS = tileInfosMapper.getAllTileById(imagesetid);
        if (tileInfosDOS.size() == 0){
            return "success";
        }
        List<Integer> tileIds = new ArrayList<>();
        for (TileInfosDO tileInfosDO : tileInfosDOS){//得到所有切片id
            tileIds.add(tileInfosDO.getId());
        }

        for (int tileId : tileIds){
            baseDataSource.deletes(tileId);//删除切片数据
        }

        return "success";
    }

    /*
    * 根据切片id批量删除
    * */
    @Override
    public void deleteImages(Integer[] tileIds) throws Exception {
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
                imageDatasetMapper.updateImageDataset(tileInfoDO.getTargetNum(), tileInfoDO.getImagesetid(), "targetNum");
            }
        }
        log.info("成功插入切片数据imagesetid:{}", tileInfoDO.getImagesetid());
    }

    @Override
    public TileSetDTO listTilesByDataSetId(TileRequestDTO tileRequestDTO) throws Exception {
        TileSetDTO tileSetDTO = new TileSetDTO();
        List<Tile> tileList = new ArrayList<>();

        int pageNo = tileRequestDTO.getPageNo() !=0 ? tileRequestDTO.getPageNo() : 1;
        int pageSize = tileRequestDTO.getPageSize() !=0 ? tileRequestDTO.getPageSize() : 10;

        Page<String> page = new Page<>();
        page.setCurrent(pageNo);
        page.setSize(pageSize);

        //分页查询结果
        IPage<TileInfosDO> tileInfosDOIPage = tileInfosMapper.listTilesByDataSetId(page, tileRequestDTO);
        int totalCount = (int) tileInfosDOIPage.getTotal();
        List<TileInfosDO> tileInfosDOS = tileInfosDOIPage.getRecords(); //切片信息集合
        if (tileInfosDOS.size() > 0){
            try {
                tileList = baseDataSource.getImages(tileInfosDOS);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (tileList != null){
                tileSetDTO.setTotalCount((int)tileInfosDOIPage.getTotal());//总数
                tileSetDTO.setTileList(tileList);
                return tileSetDTO;
            }else {
                return null;
            }
        }else {
            TileSetDTO tileSetDTO2 = new TileSetDTO();
            tileSetDTO2.setTotalCount(totalCount);
            return tileSetDTO2;
        }
    }

    /**
     *
     * @param tileId
     * @param type
     * @return
     */
    @Override
    public Tile getTileByName(String tileId, String type) throws Exception {
        Tile tile = new Tile();
        TileInfosDO tileInfosDO = tileInfosMapper.getTileByName(Integer.valueOf(tileId));
        if (tileInfosDO != null){
            tile.setName(tileInfosDO.getDataPath());
            tile.setId(tileInfosDO.getId());
            tile.setCreateTime(tileInfosDO.getCreateTime());
            if ("imgs".equals(type)) {
                tile.setBase64Tile(baseDataSource.getImageByPath(tileInfosDO.getStoragePath()));
                return tile;
            } else if ("visuals".equals(type)) {
                tile.setBase64Tile(baseDataSource.getImageByPath(tileInfosDO.getVisualPath()));
                return tile;
            } else if ("xmls".equals(type)) {
                tile.setBase64Tile(baseDataSource.getImageByPath(tileInfosDO.getLabelPath()));
                return tile;
            }else {
                throw new Exception("该切片不存在");
            }
        }
        return null;
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
