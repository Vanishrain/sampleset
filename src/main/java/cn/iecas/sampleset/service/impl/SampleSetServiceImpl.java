package cn.iecas.sampleset.service.impl;

import cn.iecas.sampleset.common.constant.SampleFileType;
import cn.iecas.sampleset.dao.SampleSetMapper;
import cn.iecas.sampleset.pojo.domain.Image;
import cn.iecas.sampleset.pojo.domain.SampleInfo;
import cn.iecas.sampleset.pojo.domain.SampleSetInfo;
import cn.iecas.sampleset.pojo.dto.*;
import cn.iecas.sampleset.pojo.dto.common.PageResult;
import cn.iecas.sampleset.pojo.entity.Statistic;
import cn.iecas.sampleset.pojo.enums.OperationType;
import cn.iecas.sampleset.pojo.enums.SampleSetStatus;
import cn.iecas.sampleset.pojo.enums.SampleType;
import cn.iecas.sampleset.service.ImageService;
import cn.iecas.sampleset.service.SampleSetService;
import cn.iecas.sampleset.service.SampleService;
import cn.iecas.sampleset.utils.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.client.ResourceAccessException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SampleSetServiceImpl extends ServiceImpl<SampleSetMapper, SampleSetInfo> implements SampleSetService {

    @Autowired
    HttpServletRequest httpServletRequest;
    
    @Value("${value.dir.rootDir}")
    private String rootDir;

    @Autowired
    private ImageService imageService;

    @Autowired
    private SampleService sampleService;

    @Override
    public void downloadSampleSet(int datasetId, HttpServletResponse response) throws IOException {
        SampleSetInfo sampleSetInfo = this.getById(datasetId);
        Assert.notNull(sampleSetInfo,"样本集不存在");
        File sampleSetFile = FileUtils.getFile(this.rootDir , sampleSetInfo.getPath());
        if (!sampleSetFile.exists())
            throw new IllegalArgumentException("样本集不存在");

        response.reset();
        response.setCharacterEncoding("utf-8");
        response.setContentType("multipart/form-data");
        String downloadName = new String(sampleSetInfo.getName().getBytes("UTF-8"),"ISO-8859-1");
        response.setHeader("Content-Disposition","attachment;fileName=\""+downloadName+"\"");

        CompressUtil.toZip(sampleSetFile.getAbsolutePath(),response.getOutputStream());
    }

    /**
     * 获取样本集的统计信息
     * @return
     */
    @Override
    public SampleSetStatistic getStatistic() {
        long totalTargetNum = 0;
        SampleSetStatistic sampleSetStatistic = new SampleSetStatistic();
        List<Statistic> statisticList = this.baseMapper.getStatistic();
        for (Statistic statistic : statisticList)
            totalTargetNum += statistic.getTargetNum();

        sampleSetStatistic.setTargetNum(totalTargetNum);
        sampleSetStatistic.setDataSetNum(statisticList.size());
        return sampleSetStatistic;
    }

    @Override
    public List<SampleSetInfo> listSampleSetInfo(int userId) {
        QueryWrapper<SampleSetInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        return this.list(queryWrapper);
    }

    @Override
    public void createSampleSet(SampleSetInfo sampleSetInfo) {
        sampleSetInfo.setStatus(SampleSetStatus.TOUPLOAD);
        sampleSetInfo.setCreateTime(DateUtil.nowDate());
        this.save(sampleSetInfo);
        String sampleSetPath = sampleSetInfo.isPublic() ? FileUtils.getStringPath("sample_set",System.currentTimeMillis())
                : FileUtils.getStringPath("user",sampleSetInfo.getUserId(),"sample_set",sampleSetInfo.getId());
        sampleSetInfo.setPath(sampleSetPath);
        this.updateById(sampleSetInfo);
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateSampleSetCount(int sampleSetId, int count, OperationType operationType) {
        SampleSetInfo sampleSetInfo = this.getById(sampleSetId);
        Assert.notNull(sampleSetInfo,"样本集不存在");

        int newCount = OperationType.PLUS == operationType ? sampleSetInfo.getCount() + count :  sampleSetInfo.getCount() - count;
        sampleSetInfo.setCount(newCount);
        this.updateById(sampleSetInfo);
    }


    /**
     * 分页获取样本集信息
     * @param sampleSetInfoRequestParam 样本集查找参数
     * @return 样本集分页信息
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, readOnly = true)
    public PageResult<SampleSetInfo> listSampleSetInfos(SampleSetInfoRequestParam sampleSetInfoRequestParam) {
        Page<SampleSetInfo> page = new Page<>(sampleSetInfoRequestParam.getPageNo(), sampleSetInfoRequestParam.getPageSize());
        IPage<SampleSetInfo> imageDataSetInfoPage = this.baseMapper.listImageDataSetInfos(page, sampleSetInfoRequestParam);
        return new PageResult<>(imageDataSetInfoPage.getCurrent(),imageDataSetInfoPage.getRecords(),imageDataSetInfoPage.getTotal());
    }



    /**
     * 批量删除指定id的样本集，以及其中的数据
     * @param sampleSetIdList 样本集列表
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteSampleSetByIds(List<Integer> sampleSetIdList) throws Exception {
        for(int sampleSetId:sampleSetIdList) {
            log.info("删除id为：{}的样本集", sampleSetId);
            this.removeById(sampleSetId);
            this.sampleService.deleteBySampleSetId(sampleSetId);
            log.info("删除样本集{}成功", sampleSetId);
        }
    }

    @Override
    public void updateSampleSetStatus(int sampleSetId, SampleSetStatus sampleSetStatus) {

    }

    /**
     * 创建样本集
     * @param sampleSetCreationInfo 样本集创建相关信息
     * @throws IOException
     */
    @Async
    @Override
    public void createSampleSet(SampleSetCreationInfo sampleSetCreationInfo,String token) throws IOException, ResourceAccessException {
        int userId = sampleSetCreationInfo.getUserId();
        SampleSetInfo sampleSet = SampleSetInfo.builder().status(SampleSetStatus.CREATING).keywords(sampleSetCreationInfo.getKeywords())
                .description(sampleSetCreationInfo.getDescription()).isSlice(sampleSetCreationInfo.isSlice())
                .createTime(DateUtil.nowDate()).name(sampleSetCreationInfo.getSampleSetName()).source(sampleSetCreationInfo.getSource()).userId(userId).build();
        this.save(sampleSet);
        int sampleSetId = sampleSet.getId();
        String sampleSetDirPath = sampleSetCreationInfo.isPublic() ?
                FileUtils.getStringPath("sample_set",sampleSet.getName()+"-"+sampleSetId) :
                FileUtils.getStringPath("user",userId,"sample_set",sampleSetId);
        sampleSet.setPath(sampleSetDirPath);

        /*
        获取manifest路径，并获取每个影像对应的标注信息
         */
        log.info("开始处理manifest文件");
        List<SampleInfo> sampleInfoList = new ArrayList<>();
        String manifestPath = FileUtils.getStringPath(this.rootDir,sampleSetCreationInfo.getManifestPath());
        String content = FileUtils.readFile(manifestPath);
        Assert.hasText(content,"manifest数据读取错误");
        Manifest manifest = JSONObject.parseObject(content, Manifest.class);
        Map<Integer, List<Manifest.Data>> dataMap = manifest.getData().stream().collect(Collectors.groupingBy(data -> data.getSource().getId()));
        List<Integer> imageIdList = manifest.getData().stream().map(Manifest.Data::getSource).map(Manifest.Data.Source::getId).collect(Collectors.toList());

        /*
        获取样本信息，并生成每个影像的xml文件，并将样本和其对应的xml放到样本集对应的位置
         */
        log.info("获取样本信息，并制作样本");
        List<Image> imageList = this.imageService.listImageInfoByIdList(imageIdList,token);
        log.info("获取影像信息成功");
        for (Image image : imageList) {
            File imageSrcFile = FileUtils.getFile(this.rootDir,image.getPath());
            if (!imageSrcFile.exists()){
                log.error("文件：{} 不存在",imageSrcFile.getAbsolutePath());
                continue;
            }

            Manifest.Data data = dataMap.get(image.getId()).get(0);
            if (!sampleSetCreationInfo.isSlice()){
                SampleInfo sampleInfo = createSample(sampleSet,image,data);
                sampleInfoList.add(sampleInfo);
            }else {
                if(data.getObjects().getObject().size()==0)
                    continue;

                List<SampleInfo> sampleInfos = createSliceSample(sampleSet,image,data);
                sampleInfoList.addAll(sampleInfos);
            }

        }

        log.info("保存样本信息，并结束创建");
        this.sampleService.saveBatch(sampleInfoList);
        sampleSet.setCount(imageList.size());
        sampleSet.setStatus(SampleSetStatus.FINISH);
        this.updateById(sampleSet);
    }

    /**
     * 无需切片时，生成样本数据
     * @param sampleSetInfo 样本集信息
     * @param image 影像信息
     * @return 样本信息
     * @throws IOException
     */
    private SampleInfo createSample(SampleSetInfo sampleSetInfo, Image image, Manifest.Data label) throws IOException {
        int sampleSetId = sampleSetInfo.getId();
        String imageFilePath = FileUtils.getStringPath(sampleSetInfo.getPath(),"imgs",image.getImageName());
        File imageSrcFile = FileUtils.getFile(this.rootDir,image.getPath());
        String fileBaseName = FilenameUtils.getBaseName(imageSrcFile.getName());
        File imageDestDir = FileUtils.getFile(this.rootDir,imageFilePath);
        String xmlLabelFilePath = FileUtils.getStringPath(sampleSetInfo.getPath(),"xml",fileBaseName+".xml");



        /*
        将原影像数据copy到样本集位置，并生成对应的标注xml文件
         */
        log.info("开始复制影像和标注信息到样本集");
        FileUtils.copyFile(imageSrcFile,imageDestDir);
        File labelXMLFile = FileUtils.getFile(this.rootDir,xmlLabelFilePath);
        FileUtils.objectToXML(Manifest.Data.class,label,labelXMLFile);

        log.info("复制影像和标注信息到样本集成功");
        return SampleInfo.builder().sampleSetId(sampleSetId).createTime(DateUtil.nowDate()).name(image.getImageName()).
                hasThumb(true).sampleThumb(image.getThumb()).bands(image.getBands()).samplePath(imageFilePath)
                .bit(image.getBit()).labelPath(xmlLabelFilePath).build();
    }

    /**
     * 创建样本切片
     * @param sampleSetInfo 样本集信息
     * @param image 影像信息
     * @param label 影像标注信息
     * @return 样本信息集合
     */
    private List<SampleInfo> createSliceSample(SampleSetInfo sampleSetInfo, Image image, Manifest.Data label) throws IOException {
        List<SampleInfo> sampleInfoList = new ArrayList<>();
        String imageBaseName = FilenameUtils.getBaseName(image.getImageName());
        String imagePath = FileUtils.getStringPath(this.rootDir,image.getPath());
        List<Manifest.Data.Objects.ObjectInfo> objectInfoList = label.getObjects().getObject();


        int count = 0;
        for (Manifest.Data.Objects.ObjectInfo objectInfo : objectInfoList) {
            count++;
            List<String> pointList = objectInfo.getPoints().getPoint();
            Assert.notEmpty(pointList,"不存在目标点");
            
            label.getObjects().setObject(JSONArray.parseArray(JSON.toJSONString(Arrays.asList(objectInfo))));
            String samplePath = FileUtils.getStringPath(sampleSetInfo.getPath(), SampleFileType.SAMPLE_IMG,imageBaseName + "_" + count + ".tif");
            String xmlFilePath = FileUtils.getStringPath(sampleSetInfo.getPath(),SampleFileType.SAMPLE_XML,imageBaseName + "_" + count + ".xml");
            String thumbPath = FileUtils.getStringPath(sampleSetInfo.getPath(), SampleFileType.SAMPLE_IMG,"thumb_"+imageBaseName + "_" + count + ".tif");

            SliceGenerateUtil.generateSlice(pointList,objectInfo.getCoordinate(),imagePath,FileUtils.getStringPath(this.rootDir,samplePath));

            String thumbnail = null;
            try {
                CreateThumbnail.CreatThumbnailByDataType(FileUtils.getStringPath(this.rootDir,samplePath),FileUtils.getStringPath(this.rootDir,thumbPath),256);
                byte[] thumbnailData = FileUtils.getImageByteArray(FileUtils.getStringPath(this.rootDir,thumbPath));
                if (thumbnailData!=null){
                    thumbnail = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(thumbnailData);
                    org.apache.commons.io.FileUtils.forceDelete(new File(FileUtils.getStringPath(this.rootDir,thumbPath)));
                }
            } catch (IOException e) {
                log.error("生成影像: {} 的缩略图失败",samplePath);
            }

            FileUtils.objectToXML(Manifest.Data.class,label,new File(FileUtils.getStringPath(this.rootDir,xmlFilePath)));
            SampleInfo sampleInfo = SampleInfo.builder().sampleSetId(sampleSetInfo.getId()).labelPath(xmlFilePath).samplePath(samplePath)
                    .bands(image.getBands()).createTime(DateUtil.nowDate()).build();
            if (thumbnail!=null){
                sampleInfo.setHasThumb(true);
                sampleInfo.setSampleThumb(thumbnail);
            }
            sampleInfoList.add(sampleInfo);
        }
        return sampleInfoList;
    }

}
