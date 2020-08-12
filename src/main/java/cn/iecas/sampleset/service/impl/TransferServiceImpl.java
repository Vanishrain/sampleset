package cn.iecas.sampleset.service.impl;

import cn.iecas.sampleset.dao.TileTransferMapper;
import cn.iecas.sampleset.datasource.BaseDataSource;
import cn.iecas.sampleset.pojo.domain.SampleSetInfo;
import cn.iecas.sampleset.pojo.domain.SampleSetTransferInfo;
import cn.iecas.sampleset.pojo.dto.request.SampleSetTransferParams;
import cn.iecas.sampleset.pojo.dto.response.SampleTransferStatus;
import cn.iecas.sampleset.pojo.enums.TransferStatus;
import cn.iecas.sampleset.service.SampleSetService;
import cn.iecas.sampleset.service.SampleService;
import cn.iecas.sampleset.service.TransferService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Date;

import static cn.iecas.sampleset.pojo.enums.TransferStatus.*;

@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class TransferServiceImpl extends ServiceImpl<TileTransferMapper, SampleSetTransferInfo> implements TransferService {

    @Value("${value.dir.rootDir}")
    private Path rootPath;  // 本地暂时保存文件的目录

    @Autowired
    BaseDataSource baseDataSource;

    @Autowired
    SampleService sampleService;

    @Autowired
    SampleSetService sampleSetService;



    /**
     * 修改上传条目的状态
     * @param sampleSetId
     * @param md5
     * @param transferStatus
     */
    @Override
    public void setTransferStatus(int sampleSetId, String md5, TransferStatus transferStatus) {
        QueryWrapper<SampleSetTransferInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("md5",md5);
        queryWrapper.eq("sample_set_id", sampleSetId);
        SampleSetTransferInfo sampleSetTransferInfo = this.baseMapper.selectOne(queryWrapper);
        sampleSetTransferInfo.setTransferStatus(transferStatus);
        this.baseMapper.updateById(sampleSetTransferInfo);
    }

    /*
    * 上传文件分片
    * */
    @Override
    public String transferTiles(SampleSetTransferParams sampleSetTransferParams, String uploadFilePath) throws Exception {
        String md5 = sampleSetTransferParams.getMd5();
        int sampleSetId = sampleSetTransferParams.getSampleSetId();
        File uploadFile = new File(uploadFilePath);
        if (!uploadFile.getParentFile().exists())
            uploadFile.getParentFile().mkdirs();

        RandomAccessFile randomAccessFile = new RandomAccessFile(uploadFilePath, "rw");
        FileChannel fileChannel = randomAccessFile.getChannel();


        long offset = sampleSetTransferParams.getChunkSize() * sampleSetTransferParams.getChunk();
        byte[] fileData = sampleSetTransferParams.getFile().getBytes();   //文件分片转为字节数组
        MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, offset, fileData.length);
        mappedByteBuffer.put(fileData);
        // 释放
        freedMappedByteBuffer(mappedByteBuffer);
        fileChannel.close();
        this.baseMapper.addChunkCount(sampleSetId,md5, sampleSetTransferParams.getChunks(),1);
        sampleSetTransferParams.setChunk(sampleSetTransferParams.getChunk() + 1);
        return uploadFilePath;
    }

//    @Override
//    public void download(int sampleSetId) throws Exception {
//        String datasetPath = this.downloadPath + File.separator + sampleSetId + File.separator + sampleSetId + ".zip";
//        File downloadFile = new File(datasetPath);
//
//        File downloadDir = downloadFile.getParentFile();
//        File imgsDownloadDir = new File(downloadDir.getAbsolutePath() + File.separator + TileType.TILE_IMG);
//        File visualsDownloadDir = new File(downloadDir.getAbsolutePath() + File.separator + TileType.TILE_VISUAL);
//        File xmlsDownloadDir = new File(downloadDir.getAbsolutePath() + File.separator + TileType.TILE_XML);
//
//        if (!imgsDownloadDir.exists())
//            imgsDownloadDir.mkdirs();
//        if (!visualsDownloadDir.exists())
//            visualsDownloadDir.mkdirs();
//        if (!xmlsDownloadDir.exists())
//            xmlsDownloadDir.mkdirs();
//        List<String> imgFileNameList = CompressUtil.getZipFileNameList(datasetPath, TileType.TILE_IMG);
//        List<SampleInfo> sampleInfoList = sampleService.getTileInfoNotInNameList(imgFileNameList);
//        List<String> deleteList = imgFileNameList.stream().filter(item->!sampleService.getNameInNameList(imgFileNameList).contains(item)).collect(Collectors.toList());
//        for (String name : deleteList) {
//            String fileName = TileType.TILE_IMG + File.separator + name;
//            CompressUtil.delete(datasetPath,fileName);
//            fileName = TileType.TILE_VISUAL + File.separator + name;
//            CompressUtil.delete(datasetPath,fileName);
//            fileName = TileType.TILE_XML + File.separator + name;
//            CompressUtil.delete(datasetPath,fileName);
//        }
//
//        for (SampleInfo sampleInfo : sampleInfoList){
//            String imgFilePath = imgsDownloadDir.getAbsolutePath() + File.separator + sampleInfo.getName();
//            byte[] data = baseDataSource.download(sampleInfo.getStoragePath());
//            FileOutputStream fileOutputStream = new FileOutputStream(imgFilePath);
//            fileOutputStream.write(data);
//
//            String visualFilePath = visualsDownloadDir.getAbsolutePath() + File.separator + sampleInfo.getName();
//            data = baseDataSource.download(sampleInfo.getStoragePath());
//            fileOutputStream = new FileOutputStream(visualFilePath);
//            fileOutputStream.write(data);
//
//            String xmlFilePath = imgsDownloadDir.getAbsolutePath() + File.separator + sampleInfo.getName();
//            data = baseDataSource.download(sampleInfo.getStoragePath());
//            fileOutputStream = new FileOutputStream(xmlFilePath);
//            fileOutputStream.write(data);
//        }
//        CompressUtil.compress(imgsDownloadDir.getAbsolutePath(),datasetPath);
//        CompressUtil.compress(visualsDownloadDir.getAbsolutePath(),datasetPath);
//        CompressUtil.compress(xmlsDownloadDir.getAbsolutePath(),datasetPath);
//
//        if (!downloadFile.exists())
//            throw new Exception("样本集：" + sampleSetId + "的下载文件不存在");
//
//
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        HttpServletResponse response = servletRequestAttributes.getResponse();
//        response.setContentType("application/octet-stream;charset=UTF-8");
//        String fileName = new String(datasetPath.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
//        response.setHeader("Content-disposition", "attachment;filename=" + java.net.URLEncoder.encode(String.valueOf(sampleSetId),"UTF-8"));
//        response.setHeader("Access-Control-Allow-Origin", "*");
//        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
//        OutputStream out = response.getOutputStream();
//
//        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(downloadFile));
//        byte[] buffer = new byte[1024];
//        int count = bufferedInputStream.read(buffer);
//        while(-1 != count){
//            out.write(buffer);
//            count = bufferedInputStream.read(buffer);
//        }
//        out.flush();
//        out.close();
//    }


    /**
     * 检查并修改文件上传进度,判断是否完成
     * @param sampleSetTransferParams
     * @param uploadDirPath
     * @return
     * @throws IOException
     */
    public boolean checkAndSetUploadProgress(SampleSetTransferParams sampleSetTransferParams, String uploadDirPath) throws IOException {
        String md5 = sampleSetTransferParams.getMd5();
        int sampleSetId = sampleSetTransferParams.getSampleSetId();

        SampleSetTransferInfo sampleSetTransferInfo = getSampleTransferInfoBySampleSetIdAndMD5(sampleSetId,md5);
        int chunck = sampleSetTransferInfo.getUploadedChunk();
        int chuncks = sampleSetTransferInfo.getChunks();

        return chuncks == chunck;

//        File confFile = new File(uploadDirPath + ".conf");
//
//        RandomAccessFile accessConfFile = new RandomAccessFile(confFile, "rw");
//
//        accessConfFile.setLength(tileTransferParamsDTO.getChunks());
//        accessConfFile.seek(tileTransferParamsDTO.getChunk());
//        accessConfFile.write(Byte.MAX_VALUE);
//
//        //completeList 检查是否全部完成(全部分片都成功上传)
//        byte[] completeList = FileUtils.readFileToByteArray(confFile);
//        byte isComplete = Byte.MAX_VALUE;
//
//        for (int i = 0; i < completeList.length && isComplete == Byte.MAX_VALUE; i++)
//            //与运算, 如果有部分没有完成则 isComplete 不是 Byte.MAX_VALUE
//            isComplete = (byte) (isComplete & completeList[i]);
//
//        accessConfFile.close();
//
//        if (isComplete == Byte.MAX_VALUE) { //文件分片全部上传完成
//            stringRedisTemplate.opsForHash().put(Constants.FILE_UPLOAD_STATUS, md5, "true");
//            stringRedisTemplate.opsForValue().set(Constants.FILE_MD5_KEY + md5, uploadDirPath + "/" + fileName);
//            return true;
//        } else {
//            if (!stringRedisTemplate.opsForHash().hasKey(Constants.FILE_UPLOAD_STATUS, md5)) {
//                stringRedisTemplate.opsForHash().put(Constants.FILE_UPLOAD_STATUS, md5, "false");
//            }
//            if (!stringRedisTemplate.hasKey(Constants.FILE_MD5_KEY + md5)) {
//                stringRedisTemplate.opsForValue().set(Constants.FILE_MD5_KEY + md5, uploadDirPath + "/" + fileName + ".conf");
//            }
//            return false;
//        }
    }

    /*
     * 通过MD5的值判断文件的上传状态
     * 从未上传
     * 已经上传完成
     * 上传一部分，断点续传
     * */
    public SampleTransferStatus checkFileMd5(int sampleSetId, String md5) throws Exception {
        SampleTransferStatus sampleTransferStatus = new SampleTransferStatus();
        SampleSetInfo sampleSetInfo = sampleSetService.getById(sampleSetId);
        Assert.notNull(sampleSetInfo,"将要上传数据的样本集不存在");

        int version = sampleSetInfo.getVersion() + 1;
        SampleSetTransferInfo sampleSetTransferInfo = this.getOne(new QueryWrapper<SampleSetTransferInfo>().eq("sample_set_id",sampleSetId).eq("md5",md5));

        //该文件从未上传
        if (null == sampleSetTransferInfo) {
            sampleSetTransferInfo = SampleSetTransferInfo.builder().md5(md5).transferStatus(TRANSFERING)
                    .sampleSetId(sampleSetId).version(version).createTime(new Date()).build();
            this.save(sampleSetTransferInfo);
        }

        BeanUtils.copyProperties(sampleSetTransferInfo, sampleTransferStatus);
        return sampleTransferStatus;
    }

    @Override
    public SampleSetTransferInfo getSampleTransferInfoBySampleSetIdAndMD5(int sampleSetId, String md5) {
        QueryWrapper<SampleSetTransferInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sample_set_id", sampleSetId);
        queryWrapper.eq("md5", md5);
        return this.baseMapper.selectOne(queryWrapper);
    }


    /**
     * 在MappedByteBuffer释放后再对它进行读操作的话就会引发jvm crash，在并发情况下很容易发生
     * 正在释放时另一个线程正开始读取，于是crash就发生了。所以为了系统稳定性释放前一般需要检查是否还有线程在读或写
     *
     * @param mappedByteBuffer
     */
    public static void freedMappedByteBuffer(final MappedByteBuffer mappedByteBuffer) {
        try {
            if (mappedByteBuffer == null) {
                return;
            }

            mappedByteBuffer.force();
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    try {
                        Method getCleanerMethod = mappedByteBuffer.getClass().getMethod("cleaner", new Class[0]);
                        getCleanerMethod.setAccessible(true);
                        sun.misc.Cleaner cleaner = (sun.misc.Cleaner) getCleanerMethod.invoke(mappedByteBuffer,
                                new Object[0]);
                        cleaner.clean();
                    } catch (Exception e) {
                        log.error("clean MappedByteBuffer error!!!", e);
                    }
                    log.info("clean MappedByteBuffer completed!!!");
                    return null;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
