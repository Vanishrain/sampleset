package cn.iecas.sampleset.service.impl;

import cn.iecas.sampleset.common.annotation.MethodLog;
import cn.iecas.sampleset.common.constant.SampleFileType;
import cn.iecas.sampleset.pojo.domain.SampleInfo;
import cn.iecas.sampleset.pojo.domain.SampleSetInfo;
import cn.iecas.sampleset.pojo.dto.request.SampleSetTransferParams;
import cn.iecas.sampleset.pojo.enums.SampleSetStatus;
import cn.iecas.sampleset.pojo.enums.TransferStatus;
import cn.iecas.sampleset.service.AsyncService;
import cn.iecas.sampleset.service.SampleService;
import cn.iecas.sampleset.service.SampleSetService;
import cn.iecas.sampleset.service.TransferService;
import cn.iecas.sampleset.utils.CompressUtil;
import cn.iecas.sampleset.utils.CreateThumbnail;
import cn.iecas.sampleset.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * 异步任务类
 */
@Slf4j
@Service
public class AsyncServiceImpl implements AsyncService {

    @Value("${value.dir.rootDir}")
    private String rootPath;

    @Autowired
    private SampleService sampleService;

    @Autowired
    private TransferService transferService;

    @Autowired
    private SampleSetService sampleSetService;

    private static final int FILE_SIZE = 1024 * 1024;

    //@Async
    @Override
    @MethodLog("解压缩并存储样本数据")
    public void decompressAndStorageSampleSet(SampleSetInfo sampleSetInfo, String md5, String uploadFilePath)  {
        int sampleSetId = sampleSetInfo.getId();
        String destPath = new File(uploadFilePath).getParentFile().getAbsolutePath()+File.separator+"compress";
        try{
            CompressUtil.decompress(uploadFilePath,destPath,false);
            String sampleSetSize = FileUtils.getDirectorySize(destPath);
            sampleSetInfo.setSize(sampleSetSize);
            boolean normalDir = checkAndNormalizeDir(destPath,FileUtils.getStringPath(this.rootPath,sampleSetInfo.getPath()));
            if (normalDir)
                storageTileDir(sampleSetId, FileUtils.getStringPath(rootPath,sampleSetInfo.getPath()));
            transferService.setTransferStatus(sampleSetId, md5 , TransferStatus.FINISHED);
            FileUtils.deleteDirectory(new File(uploadFilePath).getParentFile());
            sampleSetInfo.setStatus(SampleSetStatus.FINISH);
            this.sampleSetService.updateById(sampleSetInfo);
            log.info("存储样本集数据：{} 完成",uploadFilePath);
        }catch (Exception e){
            log.error("存储样本集数据：{}失败",uploadFilePath);
        }

    }

    /**
     * 将上传的样本集文件夹存储到数据库和文件系统中
     * @param desPath
     */
    private void storageTileDir(int sampleSetId, String desPath) throws IOException {
        log.info("正在存储样本集:{} 的数据，路径为:{}",sampleSetId,desPath);

        File samplesDirFile = FileUtils.getFile(desPath,SampleFileType.SAMPLE_IMG);
        Assert.notEmpty(samplesDirFile.listFiles(),"样本集数据为空");
        List<SampleInfo> sampleInfoList = new ArrayList<>();
        SampleSetInfo sampleSetInfo = sampleSetService.getById(sampleSetId);
        int userId = sampleSetInfo.getUserId();
        int version = sampleSetInfo.getVersion() + 1;
        for (File sampleFile : samplesDirFile.listFiles()){
            SampleInfo sampleInfo = new SampleInfo();
            sampleInfo.setHasThumb(false);
            String sampleFileBaseName = FilenameUtils.getBaseName(sampleFile.getName());
            String sampleRelativePath = sampleSetInfo.getPath();
            String xmlFilePath = FileUtils.getStringPath(this.rootPath,sampleRelativePath,SampleFileType.SAMPLE_XML,sampleFileBaseName+".xml");

            if (sampleFile.length() > FILE_SIZE){
                String thumbnail = null;
                String sampleFilePath = FileUtils.getStringPath(this.rootPath,sampleRelativePath,SampleFileType.SAMPLE_IMG,sampleFile.getName());
                String thumbTempPath = FileUtils.getStringPath(this.rootPath,sampleRelativePath,"thumb_"+ sampleFile.getName());
                try{
                    CreateThumbnail.CreatThumbnailByDataType(sampleFilePath,thumbTempPath,512);
                    byte[] data = FileUtils.getImageByteArray(thumbTempPath);
                    if (data!=null){
                        thumbnail = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(data);
                        //org.apache.commons.io.FileUtils.forceDelete(new File(thumbTempPath));
                    }
                }catch (Exception e){
                    System.out.println("发生异常");
                }

                sampleInfo.setHasThumb(true);
                sampleInfo.setSampleThumb(thumbnail);
            }

            if(new File(xmlFilePath).exists())
                sampleInfo.setLabelPath(FileUtils.getStringPath(sampleRelativePath,SampleFileType.SAMPLE_XML,sampleFileBaseName+".xml"));

            sampleInfo.setSamplePath(FileUtils.getStringPath(sampleRelativePath,SampleFileType.SAMPLE_IMG,sampleFile.getName()));
            sampleInfo.setName(sampleFile.getName());
            sampleInfo.setVersion(version);
            sampleInfo.setSampleSetId(sampleSetId);
            sampleInfo.setCreateTime(new Date());
            sampleInfoList.add(sampleInfo);
        }

        this.sampleService.saveBatch(sampleInfoList);
        sampleSetInfo.setVersion(sampleSetInfo.getVersion()+1);
        sampleSetInfo.setCount(sampleSetInfo.getCount() + sampleInfoList.size());
        this.sampleSetService.updateById(sampleSetInfo);
        log.info("样本集数据存储结束");
    }


    /**
     * 将压缩包中的有效内容拷贝到样本集存储的位置
     * @param dirPath
     * @throws IOException
     */
    private boolean checkAndNormalizeDir(String dirPath,String sampleSetPath) throws IOException {
        boolean normalDir = false;
        File dirPathFile = new File(dirPath);
        File sampleSetFile = new File(sampleSetPath);


        for (File childFile : dirPathFile.listFiles()) {
            String chileFileName = childFile.getName();
            if (chileFileName.equals(SampleFileType.SAMPLE_IMG.toLowerCase())||
                    chileFileName.equals(SampleFileType.SAMPLE_VISUAL.toLowerCase())||
                    chileFileName.equals(SampleFileType.SAMPLE_XML.toLowerCase()))
                normalDir = true;

            try{
                if (childFile.isDirectory())
                    FileUtils.moveDirectoryToDirectory(childFile,sampleSetFile);
                else
                    FileUtils.moveFileToDirectory(childFile,sampleSetFile);
            } catch (FileExistsException fileExistsException){
                continue;
            }

        }
        return normalDir;
    }

    public static void main(String[] args) {
        long size = org.apache.commons.io.FileUtils.sizeOfDirectory(new File("D:\\Data\\电子数据样本集"));
        System.out.println(size);

    }
}
