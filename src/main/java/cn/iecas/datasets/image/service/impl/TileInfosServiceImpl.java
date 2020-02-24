package cn.iecas.datasets.image.service.impl;

import cn.iecas.datasets.image.common.constant.TileType;
import cn.iecas.datasets.image.dao.TileInfosMapper;
import cn.iecas.datasets.image.datasource.BaseDataSource;
import cn.iecas.datasets.image.pojo.domain.ImageDataSetInfoDO;
import cn.iecas.datasets.image.pojo.domain.TileInfosDO;
import cn.iecas.datasets.image.pojo.dto.*;
import cn.iecas.datasets.image.pojo.dto.request.TileTransferParamsDTO;
import cn.iecas.datasets.image.pojo.entity.DatasetTileInfoStatistic;
import cn.iecas.datasets.image.pojo.entity.Tile;
import cn.iecas.datasets.image.pojo.entity.TileInfoStatistic;
import cn.iecas.datasets.image.pojo.domain.TileTransferInfoDO;
import cn.iecas.datasets.image.pojo.entity.uploadFile.TransferStatus;
import cn.iecas.datasets.image.service.ImageDataSetsService;
import cn.iecas.datasets.image.service.TransferService;
import cn.iecas.datasets.image.service.TileInfosService;
import cn.iecas.datasets.image.utils.CompressUtil;
import cn.iecas.datasets.image.utils.FastDFSUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class TileInfosServiceImpl extends ServiceImpl<TileInfosMapper, TileInfosDO> implements TileInfosService {


    @Value("${value.dir.monitorDir}")
    private Path rootPath;  // 本地暂时保

    @Value("${value.dir.downloadDir}")
    private String downloadPath;    //下载路径

    @Autowired
    BaseDataSource baseDataSource;

    @Autowired
    TransferService transferService;

    @Autowired
    TileInfosMapper tileInfosMapper;

    @Autowired
    ImageDataSetsService imageDataSetsService;

    private static final int BATH_INSERT_THRESHOLD = 200;


    @Override
    public void deleteByImageDatasetId(int imagesetid) throws Exception {
        this.baseMapper.deleteByImagesetid(imagesetid);
        List<TileInfosDO> tileInfosDOS = tileInfosMapper.getAllTileById(imagesetid);
        if (tileInfosDOS.size() != 0){
            for (TileInfosDO tileInfosDO : tileInfosDOS){ //得到所有切片id
                baseDataSource.deletes(tileInfosDO.getId());
                deleteTileFromDownloadFile(tileInfosDO);
            }
        }
    }

    /*
    * 根据切片id批量删除
    * */
    /**TODO
     * 跟前端确认是否每次删除所有的tile都来自同一个数据集
     */
    @Override
    public void deleteImages(int[] tileIds) throws Exception {
        for (int tileId : tileIds){
            baseDataSource.deletes(tileId);//删除切片数据
            TileInfosDO tileInfosDO = this.baseMapper.selectById(tileId);
            int imageDataSetId = tileInfosDO.getId(); //根据切片id得到数据集id
            tileInfosMapper.deleteById(tileId);//删除切片库中信息
            deleteTileFromDownloadFile(tileInfosDO);//删除压缩包中的数据

            ImageDataSetInfoDO imageDataSetInfoDO = imageDataSetsService.getImageDatasetInfoById(imageDataSetId);
            imageDataSetInfoDO.setNumber(imageDataSetInfoDO.getNumber()-1);
            imageDataSetsService.updateImageDataSetInfoById(imageDataSetInfoDO);
        }
    }

    /**
     * 查找为压缩包中没有的tile的信息
     * @param nameList
     * @return
     */
    @Override
    public List<TileInfosDO> getTileInfoNotInNameList(List<String> nameList) {
        QueryWrapper<TileInfosDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.notIn("name",nameList);
        return this.baseMapper.selectList(queryWrapper);
    }

    /**
     * 分块上传tile压缩包，并进行解压和存储
     * @param tileTransferParamsDTO
     */
    @Override
    public void uploadTiles(TileTransferParamsDTO tileTransferParamsDTO) throws Exception {
        boolean isComplete = true;
        String md5 = tileTransferParamsDTO.getMd5();
        String fileName = tileTransferParamsDTO.getName();
        int imageDatasetId = tileTransferParamsDTO.getImagesetid();
        String uploadFilePath = rootPath + File.separator + md5 + File.separator + fileName;
        ImageDataSetInfoDO imageDataSetInfo = imageDataSetsService.getImageDatasetInfoById(imageDatasetId);
        TileTransferInfoDO tileTransferInfoDO = transferService.getTileTransferInfoByImageDatasetIdAndMD5(imageDatasetId,md5);

        if (null == imageDataSetInfo)
            throw new Exception("数据集id: " + imageDatasetId + " 不存在");
        if (null == transferService.getTileTransferInfoByImageDatasetIdAndMD5(imageDatasetId,md5))
            throw new Exception("请先对文件：" + tileTransferParamsDTO.getName() + "进行md5检查");
        if ((tileTransferInfoDO.getChunks() != tileTransferInfoDO.getChunk())
            || (tileTransferInfoDO.getChunks()==0)){
            uploadFilePath = transferService.transferTiles(tileTransferParamsDTO,uploadFilePath);
            isComplete = transferService.checkAndSetUploadProgress(tileTransferParamsDTO, uploadFilePath);
        }

        if (isComplete && (TransferStatus.TRANSFERING == tileTransferInfoDO.getTransferStatus())){
            String desPath = CompressUtil.decompress(uploadFilePath,null);
            storageTileDir(imageDatasetId, desPath);
            combineToDownloadFile(imageDatasetId,desPath);
            transferService.setTransferStatus(imageDatasetId, md5 , TransferStatus.FINISHED);
            transferService.deleteFile(md5);
        }

    }

    @Override
    public TileSetDTO listTilesByDataSetId(TileRequestDTO tileRequestDTO) {
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
    public Tile getTileByType(int tileId, String type) throws Exception {
        Tile tile = new Tile();
        TileInfosDO tileInfosDO = tileInfosMapper.getTileByName(tileId);
        if (null == tileInfosDO)
            return tile;

        String imagePath;
        BeanUtils.copyProperties(tileInfosDO,tile);
        switch (type){
            case TileType.TILE_IMG :
                imagePath = tileInfosDO.getStoragePath();
                break;
            case TileType.TILE_VISUAL :
                imagePath = tileInfosDO.getVisualPath();
                break;
            case TileType.TILE_XML :
                imagePath = tileInfosDO.getLabelPath();
                break;
            default:
                throw new Exception("切片文件类型：" + type + " 不存在");
        }

        tile.setBase64Tile(baseDataSource.getImageByPath(imagePath));
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


    /**
     * 将本次上传的文件与原有数据进行合并压缩
     * @param imageDatasetId
     * @param dirPath
     */
    private void combineToDownloadFile(int imageDatasetId, String dirPath){
        log.info("正在将样本集:{} 的{}数据合并到下载目录，");
        File desFile = new File(dirPath);
        String downloadPath = this.downloadPath + File.separator + imageDatasetId;
        String filePath = downloadPath+File.separator+imageDatasetId+".zip";
        File downloadDir = new File(downloadPath);
        if (!downloadDir.exists())
            downloadDir.mkdirs();

        for (File file : desFile.listFiles()){
            String fileName = file.getName();
            if (!file.isDirectory())
                continue;

            if (fileName.equals(TileType.TILE_IMG) || fileName.equals(TileType.TILE_VISUAL) ||
                    fileName.equals(TileType.TILE_XML))
                CompressUtil.compress(file.getAbsolutePath(),filePath);
        }
        log.info("数据合并结束");
    }

    /**
     * 将上传的样本集文件夹存储到数据库和文件系统中
     * @param desPath
     */
    private void storageTileDir(int imageDatasetId, String desPath){
        log.info("正在存储样本集:{} 的数据，路径为:{}",imageDatasetId,desPath);
        int total = 0 ;
        int count = 0 ;
        String imgsDirPath = desPath + File.separator + TileType.TILE_IMG;
        File imgsDirFiles = new File(imgsDirPath);
        List<TileInfosDO> tileInfosDOList = new ArrayList<>();
        ImageDataSetInfoDO imageDataSetInfoDO = imageDataSetsService.getImageDatasetInfoById(imageDatasetId);
        int version = imageDataSetInfoDO.getVersion() + 1;
        for (File imgFile : imgsDirFiles.listFiles()){
            TileInfosDO tileInfosDO = new TileInfosDO();
            File rootFile = imgFile.getParentFile().getParentFile();
            String fileName = imgFile.getName().substring(0,imgFile.getName().lastIndexOf("."));
            String vislauFilePath = rootFile.getAbsolutePath() + File.separator + TileType.TILE_VISUAL + File.separator +  imgFile.getName();
            String xmlFilePath = rootFile.getAbsolutePath() + File.separator + TileType.TILE_XML + File.separator + fileName + ".xml";
            File visualFile = new File(vislauFilePath);
            File xmlFile = new File (xmlFilePath);

            tileInfosDO.setStoragePath(FastDFSUtil.upload(imgFile));
            if (visualFile.exists()){
                tileInfosDO.setVisualPath(FastDFSUtil.upload(visualFile));
            }

            if (xmlFile.exists()){
                tileInfosDO.setLabelPath(FastDFSUtil.upload(xmlFile));
            }

            tileInfosDO.setName(imgFile.getName());
            tileInfosDO.setVersion(version);
            tileInfosDO.setImagesetid(imageDatasetId);
            tileInfosDO.setCreateTime(new Date());

            tileInfosDOList.add(tileInfosDO);
            count++;
            total++;

            if (count >= BATH_INSERT_THRESHOLD){
                tileInfosMapper.batchInsert(tileInfosDOList);
                tileInfosDOList.clear();
                count = 0;
            }
        }
        if (!tileInfosDOList.isEmpty())
            tileInfosMapper.batchInsert(tileInfosDOList);

        imageDataSetInfoDO.setVersion(imageDataSetInfoDO.getVersion()+1);
        imageDataSetInfoDO.setNumber(imageDataSetInfoDO.getNumber() + total);
        imageDataSetsService.updateImageDataSetInfoById(imageDataSetInfoDO);
        log.info("样本集数据存储结束");
    }

    /**
     * 从下载的压缩文件中删除该文件
     * @param tileInfosDO
     */
    private void deleteTileFromDownloadFile(TileInfosDO tileInfosDO){
        String name = tileInfosDO.getName();
        int imageDatasetId = tileInfosDO.getImagesetid();
        String downloadFilePath = this.downloadPath + File.separator + imageDatasetId + File.separator + imageDatasetId + ".zip";
        File downloadFile = new File(downloadFilePath);

        if (!downloadFile.exists())
            return;
        String imgFile = TileType.TILE_IMG + File.separator + name;
        String visualFile = TileType.TILE_VISUAL + File.separator + name;
        String xmlFile = TileType.TILE_XML + File.separator + name.substring(0,name.lastIndexOf("."));
        CompressUtil.delete(downloadFilePath,imgFile);
        CompressUtil.delete(downloadFilePath,visualFile);
        CompressUtil.delete(downloadFilePath,xmlFile);
    }
}
