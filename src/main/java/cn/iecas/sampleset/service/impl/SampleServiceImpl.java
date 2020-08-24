package cn.iecas.sampleset.service.impl;

import cn.iecas.sampleset.dao.SampleInfoMapper;
import cn.iecas.sampleset.datasource.BaseDataSource;
import cn.iecas.sampleset.pojo.domain.SampleInfo;
import cn.iecas.sampleset.pojo.domain.SampleSetInfo;
import cn.iecas.sampleset.pojo.dto.*;
import cn.iecas.sampleset.pojo.dto.common.PageResult;
import cn.iecas.sampleset.pojo.dto.request.SampleSetTransferParams;
import cn.iecas.sampleset.pojo.entity.DatasetTileInfoStatistic;
import cn.iecas.sampleset.pojo.entity.Sample;
import cn.iecas.sampleset.pojo.entity.TileInfoStatistic;
import cn.iecas.sampleset.pojo.domain.SampleSetTransferInfo;
import cn.iecas.sampleset.pojo.enums.SampleSetStatus;
import cn.iecas.sampleset.pojo.enums.TransferStatus;
import cn.iecas.sampleset.pojo.enums.OperationType;
import cn.iecas.sampleset.pojo.enums.SampleType;
import cn.iecas.sampleset.service.AsyncService;
import cn.iecas.sampleset.service.SampleSetService;
import cn.iecas.sampleset.service.TransferService;
import cn.iecas.sampleset.service.SampleService;
import cn.iecas.sampleset.utils.FileUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@Transactional
public class SampleServiceImpl extends ServiceImpl<SampleInfoMapper, SampleInfo> implements SampleService {
    @Value("${value.dir.rootDir}")
    private Path rootPath;  // 本地暂时保

    @Lazy
    @Autowired
    AsyncService asyncService;

    @Autowired
    BaseDataSource baseDataSource;

    @Autowired
    TransferService transferService;

    @Autowired
    SampleSetService sampleSetService;




    @Override
    public void deleteBySampleSetId(int sampleSetId) throws Exception {
        QueryWrapper<SampleInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sample_set_id",sampleSetId);

        List<SampleInfo> sampleInfos = this.list(queryWrapper);

        for (SampleInfo sampleInfo : sampleInfos){ //得到所有切片id
            baseDataSource.deletes(sampleInfo);
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
        }
        this.sampleSetService.updateSampleSetCount(sampleSetId,deleteCount, OperationType.MINUS);

    }


    /**
     * 分块上传tile压缩包，并进行解压和存储
     * @param sampleSetTransferParams
     */
    @Override
    public SampleSetTransferParams uploadTiles(SampleSetTransferParams sampleSetTransferParams) throws Exception {
        boolean isComplete = true;
        String md5 = sampleSetTransferParams.getMd5();
        String fileName = sampleSetTransferParams.getName();
        int sampleSetId = sampleSetTransferParams.getSampleSetId();
        SampleSetInfo sampleSetInfo = this.sampleSetService.getById(sampleSetId);
        Assert.notNull(sampleSetInfo,"样本集不存在");
        String uploadFilePath = FileUtils.getStringPath(rootPath,sampleSetInfo.getPath(),md5,fileName);
        SampleSetTransferInfo sampleSetTransferInfo = this.transferService.getSampleTransferInfoBySampleSetIdAndMD5(sampleSetId,md5);
        Assert.notNull(sampleSetInfo,"数据集不存在");
        Assert.notNull(sampleSetTransferInfo,"请先对文件进行md5检查");

        if ((sampleSetTransferInfo.getChunks() != sampleSetTransferInfo.getUploadedChunk())
            || (sampleSetTransferInfo.getUploadedChunk()==0)){
            uploadFilePath = transferService.transferTiles(sampleSetTransferParams,uploadFilePath);
            isComplete = transferService.checkAndSetUploadProgress(sampleSetTransferParams, uploadFilePath);
        }

        if (isComplete && (TransferStatus.FINISHED != sampleSetTransferInfo.getTransferStatus())){
            this.transferService.setTransferStatus(sampleSetId,md5,TransferStatus.STORAGING);
            this.asyncService.decompressAndStorageSampleSet(sampleSetInfo,md5,uploadFilePath);
        }
        sampleSetTransferParams.setFile(null);
        return sampleSetTransferParams;
    }

    @Override
    public PageResult<Sample> listSamplesBySetId(SampleRequestParams sampleRequestParams) throws Exception {
        Page<String> page = new Page<>(sampleRequestParams.getPageNo(), sampleRequestParams.getPageSize());
        IPage<SampleInfo> sampleInfosIPage = this.baseMapper.listSampleInfos(page, sampleRequestParams);
        List<SampleInfo> sampleInfos = sampleInfosIPage.getRecords();
        List<Sample> sampleList = sampleInfos.isEmpty() ? new ArrayList<>() : this.baseDataSource.getImages(sampleInfos);
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
                sampleInfo.getSamplePath() : sampleInfo.getVisualPath();
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


//    /**
//     * 将上传的样本集文件夹存储到数据库和文件系统中
//     * @param desPath
//     */
//    private void storageTileDir(int sampleSetId, String desPath) throws IOException {
//        log.info("正在存储样本集:{} 的数据，路径为:{}",sampleSetId,desPath);
//
//        File samplesDirFile = FileUtils.getFile(desPath,SampleFileType.SAMPLE_IMG);
//        Assert.notEmpty(samplesDirFile.listFiles(),"样本集数据为空");
//        List<SampleInfo> sampleInfoList = new ArrayList<>();
//        SampleSetInfo sampleSetInfo = sampleSetService.getById(sampleSetId);
//        int userId = sampleSetInfo.getUserId();
//        int version = sampleSetInfo.getVersion() + 1;
//        for (File sampleFile : samplesDirFile.listFiles()){
//            SampleInfo sampleInfo = new SampleInfo();
//            sampleInfo.setHasThumb(false);
//            String sampleFileBaseName = FilenameUtils.getBaseName(sampleFile.getName());
//            String sampleRelativePath = FileUtils.getStringPath(userId,"sample_set",sampleSetId);
//            String xmlFilePath = FileUtils.getStringPath(this.rootPath,sampleRelativePath,SampleFileType.SAMPLE_XML,sampleFileBaseName+".xml");
//
//            if (sampleFile.length() > FILE_SIZE){
//                String thumbnail = null;
//                String sampleFilePath = FileUtils.getStringPath(this.rootPath,sampleRelativePath,SampleFileType.SAMPLE_IMG,sampleFile.getName());
//                String thumbTempPath = FileUtils.getStringPath(this.rootPath,sampleRelativePath,"thumb_"+ sampleFile.getName());
//                try{
//                    CreateThumbnail.CreatThumbnailByDataType(sampleFilePath,thumbTempPath,512);
//                    byte[] data = FileUtils.getImageByteArray(thumbTempPath);
//                    if (data!=null){
//                        thumbnail = "data:image/png;base64," + Base64.getEncoder().encodeToString(data);
//                        //org.apache.commons.io.FileUtils.forceDelete(new File(thumbTempPath));
//                    }
//                }catch (Exception e){
//                    System.out.println("发生异常");
//                }
//
//                sampleInfo.setHasThumb(true);
//                sampleInfo.setSampleThumb(thumbnail);
//            }
//
//            if(new File(xmlFilePath).exists())
//                sampleInfo.setLabelPath(FileUtils.getStringPath(sampleRelativePath,SampleFileType.SAMPLE_VISUAL,sampleFileBaseName+".xml"));
//
//            sampleInfo.setSamplePath(FileUtils.getStringPath(sampleRelativePath,SampleFileType.SAMPLE_XML,sampleFile.getName()));
//            sampleInfo.setName(sampleFile.getName());
//            sampleInfo.setVersion(version);
//            sampleInfo.setSampleSetId(sampleSetId);
//            sampleInfo.setCreateTime(new Date());
//            sampleInfoList.add(sampleInfo);
//        }
//
//        this.saveBatch(sampleInfoList);
//        sampleSetInfo.setVersion(sampleSetInfo.getVersion()+1);
//        sampleSetInfo.setCount(sampleSetInfo.getCount() + sampleInfoList.size());
//        this.sampleSetService.updateById(sampleSetInfo);
//        log.info("样本集数据存储结束");
//    }
//
//
//    @Async
//    public void decompressAndStorageSampleSet(int userId, String md5, int sampleSetId, String uploadFilePath) throws Exception {
//        String destPath = new File(uploadFilePath).getParentFile().getAbsolutePath();
//        CompressUtil.decompress(uploadFilePath,destPath,false);
//        checkAndNormalizeDir(destPath);
//
//        storageTileDir(sampleSetId, FileUtils.getStringPath(rootPath,userId,"sample_set",sampleSetId));
//        transferService.setTransferStatus(sampleSetId, md5 , TransferStatus.FINISHED);
//        FileUtils.deleteDirectory(new File(uploadFilePath).getParentFile());
//    }
//
//
//    /**
//     * 将压缩包中的有效内容拷贝到样本集存储的位置
//     * @param dirPath
//     * @throws IOException
//     */
//    private void checkAndNormalizeDir(String dirPath) throws IOException {
//        File backupPath = new File(dirPath);
//        File dirPathFile = new File(dirPath);
//
//        if (dirPathFile.list().length ==1 && !dirPathFile.list()[0].equals(SampleFileType.SAMPLE_IMG))
//            dirPathFile = dirPathFile.listFiles()[0];
//
//
//        for (File childFile : dirPathFile.listFiles()) {
//            String chileFileName = childFile.getName();
//            if (chileFileName.equals(SampleFileType.SAMPLE_IMG.toLowerCase())||
//                    chileFileName.equals(SampleFileType.SAMPLE_VISUAL.toLowerCase())||
//                    chileFileName.equals(SampleFileType.SAMPLE_XML.toLowerCase())) {
//                FileUtils.moveDirectoryToDirectory(childFile,backupPath.getParentFile());
//
//            }
//        }
//    }



}
