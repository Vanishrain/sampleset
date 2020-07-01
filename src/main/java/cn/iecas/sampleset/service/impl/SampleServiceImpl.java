package cn.iecas.sampleset.service.impl;

import cn.iecas.sampleset.common.constant.TileType;
import cn.iecas.sampleset.dao.SampleInfoMapper;
import cn.iecas.sampleset.datasource.BaseDataSource;
import cn.iecas.sampleset.pojo.domain.SampleInfo;
import cn.iecas.sampleset.pojo.domain.SampleSetInfo;
import cn.iecas.sampleset.pojo.dto.*;
import cn.iecas.sampleset.pojo.dto.common.PageResult;
import cn.iecas.sampleset.pojo.dto.request.TileTransferParams;
import cn.iecas.sampleset.pojo.entity.DatasetTileInfoStatistic;
import cn.iecas.sampleset.pojo.entity.Sample;
import cn.iecas.sampleset.pojo.entity.TileInfoStatistic;
import cn.iecas.sampleset.pojo.domain.SampleTransferInfo;
import cn.iecas.sampleset.pojo.enums.TransferStatus;
import cn.iecas.sampleset.pojo.enums.OperationType;
import cn.iecas.sampleset.pojo.enums.SampleType;
import cn.iecas.sampleset.service.SampleSetService;
import cn.iecas.sampleset.service.TransferService;
import cn.iecas.sampleset.service.SampleService;
import cn.iecas.sampleset.utils.CompressUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

@Slf4j
@Service
@Transactional
public class SampleServiceImpl extends ServiceImpl<SampleInfoMapper, SampleInfo> implements SampleService {
    @Value("${value.dir.monitorDir}")
    private Path rootPath;  // 本地暂时保

    @Value("${value.dir.downloadDir}")
    private String downloadPath;    //下载路径

    private String dataPath;

    private static final int BATH_INSERT_THRESHOLD = 200;

    @Autowired
    BaseDataSource baseDataSource;

    @Autowired
    TransferService transferService;

    @Autowired
    SampleInfoMapper sampleInfoMapper;

    @Autowired
    SampleSetService sampleSetService;



    @Override
    public void deleteBySampleSetId(int sampleSetId) throws Exception {
        QueryWrapper<SampleInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sample_set_id",sampleSetId);

        List<SampleInfo> sampleInfos = this.list(queryWrapper);

        if (sampleInfos.size() != 0){
            for (SampleInfo sampleInfo : sampleInfos){ //得到所有切片id
                baseDataSource.deletes(sampleInfo);
                deleteTileFromDownloadFile(sampleInfo);
            }
        }
        this.remove(queryWrapper);
    }

    /**
     * 更具样本id列表批量删除样本数据
     * @param sampleIdList 样本数据列表
     * @throws Exception
     * @// TODO: 2020/6/22 从压缩包中删除的逻辑是否改变
     */
    @Override
    public void deleteSamples(List<Integer> sampleIdList) throws Exception {
        int sampleSetId = 0;
        int deleteCount = 0;
        for (int sampleId : sampleIdList){
            SampleInfo sampleInfo = this.getById(sampleId);
            if (sampleInfo == null)
                continue;
            else
                deleteCount++;

            this.removeById(sampleId);//删除切片库中信息
            sampleSetId = sampleInfo.getSampleSetId(); //根据切片id得到数据集id
            baseDataSource.deletes(sampleInfo);//删除切片数据
            deleteTileFromDownloadFile(sampleInfo);//删除压缩包中的数据
        }
        this.sampleSetService.updateSampleSetCount(sampleSetId,deleteCount, OperationType.MINUS);

    }

    /**
     * 查找为压缩包中没有的tile的信息
     * @param nameList
     * @return
     */
    @Override
    public List<SampleInfo> getTileInfoNotInNameList(List<String> nameList) {
        QueryWrapper<SampleInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.notIn("name",nameList);
        return this.baseMapper.selectList(queryWrapper);
    }


    @Override
    public List<Object> getNameInNameList(List<String> nameList) {
        QueryWrapper<SampleInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("name").in("name",nameList);
        return this.baseMapper.selectObjs(queryWrapper);
    }


    /**
     * 分块上传tile压缩包，并进行解压和存储
     * @param tileTransferParams
     */
    @Override
    @Transactional
    public void uploadTiles(TileTransferParams tileTransferParams) throws Exception {
        boolean isComplete = true;
        String md5 = tileTransferParams.getMd5();
        String fileName = tileTransferParams.getName();
        int imageDatasetId = tileTransferParams.getImagesetid();
        String uploadFilePath = rootPath + File.separator + md5 + File.separator + fileName;
        SampleSetInfo sampleSetInfo = sampleSetService.getById(imageDatasetId);
        SampleTransferInfo sampleTransferInfo = this.transferService.getSampleTransferInfoBySampleSetIdAndMD5(imageDatasetId,md5);
        Assert.notNull(sampleSetInfo,"数据集不存在");
        Assert.notNull(sampleTransferInfo,"请先对文件进行md5检查");

        if ((sampleTransferInfo.getChunks() != sampleTransferInfo.getChunk())
            || (sampleTransferInfo.getChunks()==0)){
            uploadFilePath = transferService.transferTiles(tileTransferParams,uploadFilePath);
            isComplete = transferService.checkAndSetUploadProgress(tileTransferParams, uploadFilePath);
        }

        if (isComplete && (TransferStatus.TRANSFERING == sampleTransferInfo.getTransferStatus())){
            String desPath = CompressUtil.decompress(uploadFilePath,dataPath + File.separator + sampleSetInfo.getName());
            storageTileDir(imageDatasetId, desPath);
            combineToDownloadFile(imageDatasetId,desPath);
            transferService.setTransferStatus(imageDatasetId, md5 , TransferStatus.FINISHED);
            transferService.deleteFile(md5);
        }

    }

    @Override
    public PageResult<Sample> listSamplesBySetId(SampleRequestParams sampleRequestParams) throws Exception {
        Page<String> page = new Page<>(sampleRequestParams.getPageNo(), sampleRequestParams.getPageSize());
        IPage<SampleInfo> sampleInfosIPage = sampleInfoMapper.listSampleInfos(page, sampleRequestParams);
        List<SampleInfo> sampleInfos = sampleInfosIPage.getRecords();
        Assert.notEmpty(sampleInfos,"没有找到样本数据");

        List<Sample> sampleList = this.baseDataSource.getImages(sampleInfos);
        return new PageResult<>(sampleInfosIPage.getCurrent(),sampleList,sampleInfosIPage.getTotal());
    }

    /**
     * 通过类型和id获取对应的样本数据
     * @param sampleId 样本id
     * @param sampleType 样本类型
     * @return 样本信息
     */
    @Override
    public Sample getSampleByTypeAndId(int sampleId, SampleType sampleType) throws Exception {
        Sample sample = new Sample();
        SampleInfo sampleInfo = this.getById(sampleId);
        Assert.notNull(sample,"样本数据不存在");

        BeanUtils.copyProperties(sampleInfo, sample);
        String filePath = sampleType == SampleType.ORIGINAL ?
                sampleInfo.getStoragePath() : sampleInfo.getVisualPath();
        sample.setSampleThumb(baseDataSource.getImageByPath(filePath));
        return sample;
}


    /**
     * 根据数据集id返回
     * 返回属于该id的切片的年月日数据增长信息，当不指定imagesetidPre是则默认返回全部。
     * @param tileInfoStatParamsDTO
     * @return
     */
    @Override
    public TileInfoAllStatisticResponseDTO getStatistic(TileInfoStatParamsDTO tileInfoStatParamsDTO) {
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
        List<SampleInfo> sampleInfoList = new ArrayList<>();
        SampleSetInfo sampleSetInfo = sampleSetService.getById(imageDatasetId);
        int version = sampleSetInfo.getVersion() + 1;
        for (File imgFile : imgsDirFiles.listFiles()){
            SampleInfo sampleInfo = new SampleInfo();
            File rootFile = imgFile.getParentFile().getParentFile();
            String fileName = imgFile.getName().substring(0,imgFile.getName().lastIndexOf("."));
            String vislauFilePath = rootFile.getAbsolutePath() + File.separator + TileType.TILE_VISUAL + File.separator +  imgFile.getName();
            String xmlFilePath = rootFile.getAbsolutePath() + File.separator + TileType.TILE_XML + File.separator + fileName + ".xml";
            File visualFile = new File(vislauFilePath);
            File xmlFile = new File (xmlFilePath);
//            tileInfosDO.setStoragePath(FastDFSUtil.upload(imgFile));
//            if (visualFile.exists()){
//                tileInfosDO.setVisualPath(FastDFSUtil.upload(visualFile));
//            }
//
//            if (xmlFile.exists()){
//                tileInfosDO.setLabelPath(FastDFSUtil.upload(xmlFile));
//            }

            sampleInfo.setStoragePath(rootFile.getName() + File.separator + TileType.TILE_IMG + File.separator + imgFile.getName());
            if (visualFile.exists()){
                sampleInfo.setVisualPath(rootFile.getName() + File.separator + TileType.TILE_VISUAL + File.separator + visualFile.getName());
            }

            if (xmlFile.exists()){
                sampleInfo.setLabelPath(rootFile.getName() + File.separator + TileType.TILE_XML + File.separator + xmlFile.getName());
            }

            sampleInfo.setName(imgFile.getName());
            sampleInfo.setVersion(version);
            sampleInfo.setSampleSetId(imageDatasetId);
            sampleInfo.setCreateTime(new Date());

            sampleInfoList.add(sampleInfo);
            count++;
            total++;

            if (count >= BATH_INSERT_THRESHOLD){
                sampleInfoMapper.batchInsert(sampleInfoList);
                sampleInfoList.clear();
                count = 0;
            }
        }
        if (!sampleInfoList.isEmpty())
            sampleInfoMapper.batchInsert(sampleInfoList);

        sampleSetInfo.setVersion(sampleSetInfo.getVersion()+1);
        sampleSetInfo.setCount(sampleSetInfo.getCount() + total);
        this.sampleSetService.updateById(sampleSetInfo);
        log.info("样本集数据存储结束");
    }

    /**
     * 从下载的压缩文件中删除该文件
     * @param sampleInfo
     */
    private void deleteTileFromDownloadFile(SampleInfo sampleInfo){
        String name = sampleInfo.getName();
        int imageDatasetId = sampleInfo.getSampleSetId();
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
