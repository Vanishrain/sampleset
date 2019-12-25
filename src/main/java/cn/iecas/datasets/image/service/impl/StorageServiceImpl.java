package cn.iecas.datasets.image.service.impl;

import cn.iecas.datasets.image.dao.ImageDatasetMapper;
import cn.iecas.datasets.image.dao.TileInfosMapper;
import cn.iecas.datasets.image.pojo.domain.ImageDataSetInfoDO;
import cn.iecas.datasets.image.pojo.domain.TileInfosDO;
import cn.iecas.datasets.image.pojo.dto.CommonResponseDTO;
import cn.iecas.datasets.image.pojo.entity.uploadFile.MultipartFileParam;
import cn.iecas.datasets.image.service.StorageService;
import cn.iecas.datasets.image.utils.CompressUtil;
import cn.iecas.datasets.image.utils.Constants;
import cn.iecas.datasets.image.utils.FastDFSUtil;
import cn.iecas.datasets.image.utils.FileMD5Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.csource.fastdfs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class StorageServiceImpl implements StorageService {

    private final Logger logger = LoggerFactory.getLogger(StorageServiceImpl.class);

    @Value("${value.dir.monitorDir}")
    private Path rootPath;  // 本地保存文件的目录

    @Value("${breakpoint.upload.chunkSize}")
    private long CHUNK_SIZE;    //文件分块大小(必须与前端设定的值一致)

    @Value("#{'${value.postfix.img}'.split(',')}")
    private List<String> imageFilePostfix; //影像文件的格式

    @Value("${value.decompressCmd.rar.decompress}")
    private String decompressRarCmd; //解压缩rar命令
    @Value("${value.decompressCmd.rar.list}")
    private String decompressRarList; //解压缩列出rar文件名

    @Value("${value.decompressCmd.zip.decompress}")
    private String decompressZipCmd;//解压缩zip命令
    @Value("${value.decompressCmd.zip.list}")
    private String decompressZipList;//解压缩列出zip文件名

    @Value("${value.decompressCmd.tar.decompress}")
    private String decompressTarCmd;//解压缩tar命令
    @Value("${value.decompressCmd.tar.list}")
    private String decompressTarList;//解压缩列出tar文件名

    @Value("${value.fastdfsServer}")
    private String fastdfsServer;   //FastDFS服务路径
    @Value("${value.downloadPath}")
    private String downloadPath;    //下载路径

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private TileInfosMapper tileInfosMapper;
    @Autowired
    private ImageDatasetMapper imageDatasetMapper;
    @Autowired
    SqlSessionFactory sqlSessionFactory;

    @Autowired
    public StorageServiceImpl(@Value("${value.dir.monitorDir}") String location) { //获取文件上传路径
        this.rootPath = Paths.get(location);
    }

    /*
    * 上传文件
    * */
    @Override
    public Object uploadTiles(MultipartFileParam param, HttpServletRequest request) {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        CommonResponseDTO commonResponseDTO = null;

        if (isMultipart) {
            try {
                uploadFileByMappedByteBuffer(param); //上传过程
                return new CommonResponseDTO().success().message("批量增加切片数据成功");
            } catch (IOException e) {
                e.printStackTrace();
                log.error("文件上传失败! {}", param.toString());

                return new CommonResponseDTO().fail().message("上传失败");
            }
        } else {
            return commonResponseDTO.fail().message("请选择文件上传");
        }
    }

    /*
    * 下载
    * */
    @Override
    public void download(TileInfosDO tileInfosDO) {
        String targetFilePath = tileInfosMapper.getStoragePath(tileInfosDO.getVisualPath());

        try {
            String fdfsConf = "src/main/resources/fdfs_client.conf";
            ClientGlobal.init(fdfsConf);
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();    //跟踪服务器
            StorageServer storageServer = trackerClient.getStoreStorage(trackerServer); //存储服务器
            StorageClient1 storageClient1 = new StorageClient1(trackerServer, storageServer);

//            String groupName = targetFilePath.substring(0, targetFilePath.indexOf("/"));
//            String fileName = targetFilePath.substring(targetFilePath.indexOf("/")+1);
//            byte[] bs = storageClient1.download_file(groupName, fileName);
            byte[] bs = storageClient1.download_file1(targetFilePath);  //根据文件id下载
            OutputStream out = new FileOutputStream(downloadPath + "\\" + tileInfosDO.getDataPath());
            out.write(bs);
            out.close();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            FastDFSUtil.closeConnection();
        }
    }

    /**
     * 检查并修改文件上传进度
     *
     * @param param
     * @param uploadDirPath
     * @return
     * @throws IOException
     */
    private boolean checkAndSetUploadProgress(MultipartFileParam param, String uploadDirPath) throws IOException {
        String fileName = param.getName();
        File confFile = new File(uploadDirPath, fileName + ".conf");
        RandomAccessFile accessConfFile = new RandomAccessFile(confFile, "rw");

        //把该分段标记为 true 表示完成
        System.out.println("set part " + param.getChunk() + " complete");

        accessConfFile.setLength(param.getChunks());
        accessConfFile.seek(param.getChunk());
        accessConfFile.write(Byte.MAX_VALUE);

        //completeList 检查是否全部完成(全部分片都成功上传)
        byte[] completeList = FileUtils.readFileToByteArray(confFile);
        byte isComplete = Byte.MAX_VALUE;

        for (int i = 0; i < completeList.length && isComplete == Byte.MAX_VALUE; i++) {
            //与运算, 如果有部分没有完成则 isComplete 不是 Byte.MAX_VALUE
            isComplete = (byte) (isComplete & completeList[i]);
            System.out.println("check part " + i + " complete?:" + completeList[i]);
        }

        accessConfFile.close();

        if (isComplete == Byte.MAX_VALUE) { //文件分片全部上传完成
            stringRedisTemplate.opsForHash().put(Constants.FILE_UPLOAD_STATUS, param.getMd5(), "true");
            stringRedisTemplate.opsForValue().set(Constants.FILE_MD5_KEY + param.getMd5(), uploadDirPath + "/" + fileName);

            return true;
        } else {
            if (!stringRedisTemplate.opsForHash().hasKey(Constants.FILE_UPLOAD_STATUS, param.getMd5())) {
                stringRedisTemplate.opsForHash().put(Constants.FILE_UPLOAD_STATUS, param.getMd5(), "false");
            }
            if (!stringRedisTemplate.hasKey(Constants.FILE_MD5_KEY + param.getMd5())) {
                stringRedisTemplate.opsForValue().set(Constants.FILE_MD5_KEY + param.getMd5(), uploadDirPath + "/" + fileName + ".conf");
            }

            return false;
        }
    }

    /**
     * 文件重命名
     *
     * @param toBeRenamed   将要修改名字的文件
     * @param toFileNewName 新的名字
     * @return
     */
    public boolean renameFile(File toBeRenamed, String toFileNewName) {
        //检查要重命名的文件是否存在，是否是文件
        if (!toBeRenamed.exists() || toBeRenamed.isDirectory()) {
            logger.info("File does not exist: " + toBeRenamed.getName());

            return false;
        }

        String p = toBeRenamed.getParent();
        File newFile = new File(p + File.separatorChar + toFileNewName);

        //修改文件名
        return toBeRenamed.renameTo(newFile);
    }

    /**
     * 根据rar，zip，tar三种不同类型的文件
     * 获取相应的解压缩命令
     */
    private String getDecompressCmdPrefix(String fileName){
        if(FilenameUtils.isExtension(fileName,"rar") )
            return  decompressRarCmd;
        if (FilenameUtils.isExtension(fileName,"zip"))
            return decompressZipCmd;

        return decompressTarCmd;
    }


    /**
     * 根据rar，zip，tar三种不同的类型的文件
     * 获取相应的查询文件内容的命令
     */
    private String getDecompressCmdListPrefix(String fileName){
        if(FilenameUtils.isExtension(fileName,"rar") )
            //compressCmdList=String.format("%s %s",this.jobInfo.getDecompressRarList(),srcPath);
            return decompressRarList;

        if (FilenameUtils.isExtension(fileName,"zip"))
            //compressCmdList=String.format("%s %s ",this.jobInfo.getDecompressZipList(),srcPath);
            return decompressZipList;

        //compressCmdList=String.format("%s %s ",this.jobInfo.getDecompressTarList(),srcPath);
        return decompressTarList;
    }

    /*
     * 上传文件过程
     * */
    private Object uploadFileByMappedByteBuffer(MultipartFileParam param) throws IOException {
        String fileName = param.getName();
        String uploadDirPath = rootPath + "\\" + param.getMd5();   //上传文件所在目录
        String decompressDirPath = uploadDirPath;  //解压文件所在路径
        String tempFileName = fileName + "_temp";
        File tmpDir = new File(uploadDirPath);
        File tmpFile = new File(uploadDirPath, tempFileName);

        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }

        RandomAccessFile tempRaf = new RandomAccessFile(tmpFile, "rw");
        FileChannel fileChannel = tempRaf.getChannel();

        /*
         * 写入分片数据
         */
        long offset = CHUNK_SIZE * param.getChunk();
        byte[] fileData = param.getFile().getBytes();   //文件分片转为字节数组
        MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, offset, fileData.length);
        mappedByteBuffer.put(fileData);
        // 释放
        FileMD5Util.freedMappedByteBuffer(mappedByteBuffer);
        fileChannel.close();

        boolean isOk = checkAndSetUploadProgress(param, uploadDirPath); //检查并修改文件上传进度
        if (isOk) { //分片上传完成
            boolean flag = renameFile(tmpFile, fileName);   //临时文件重命名
            FileUtils.forceDelete(new File(uploadDirPath, fileName + ".conf")); //删除进度文件
            System.out.println("upload complete !!" + flag + " name=" + fileName);

            /*
             * 解压
             * */
            String decompressCmdPrefix = getDecompressCmdPrefix(fileName);  //解压命令前缀
            String decompressCmdListPrefix = getDecompressCmdListPrefix(fileName);  //解压列表命令前缀
            String srcPath =  tmpDir + "\\" + fileName;
            String destPath = decompressDirPath;
            //获得压缩文件列表
            List<String> decompressedFile = CompressUtil.decompress(srcPath, destPath,
                    decompressCmdPrefix,    //解压
                    decompressCmdListPrefix,    //列表
                    imageFilePostfix);

            /*
             * 上传至服务器
             * */
            File file = null;
            TileInfosDO tileInfosDO = new TileInfosDO();
            ImageDataSetInfoDO imageDataSetInfoDO = imageDatasetMapper.getImageDataSetById(35);
            int fileFileCount = imageDataSetInfoDO.getNumber(); //上传文件数量
            FileInputStream imgFis = null;
            FileInputStream visualFis = null;
            FileInputStream xmlFis = null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            String tileFileRootDirPath = decompressDirPath + "\\car";   //"D:\\JCP\\YXCP_YGYX\\IMG\\d2d540edd0a8cd41071aad778b98a6e3\\car";
            String imgsDir = tileFileRootDirPath + "\\imgs";
            String visualDir = tileFileRootDirPath + "\\visual";
            String xmlsDir = tileFileRootDirPath + "\\xmls";

            File[] imgs = new File(imgsDir).listFiles();
            int len = imgs.length;
//            List<String> storagePath = new ArrayList<>();
//            List<String> visualPath = new ArrayList<>();
//            List<String> labelPath = new ArrayList<>();
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            for (int i=0; i<len; i++){
                File imgFile = imgs[i]; //img
                String imgName = imgFile.getName();
                String commonName = imgName.substring(0, imgName.lastIndexOf("."));

                File visualFile = new File(visualDir + "\\" + commonName + ".jpg");
                File xmlFile = new File(xmlsDir + "\\" + commonName + ".xml");

                /*
                * 上传文件
                * */
                imgFis = new FileInputStream(imgFile);
                visualFis = new FileInputStream(visualFile);
                xmlFis = new FileInputStream(xmlFile);
//                String storagePath = (String) FastDFSUtil.upload(imgFile, imgFis, baos);
//                String visualPath = (String) FastDFSUtil.upload(visualFile, visualFis, baos);
//                String labelPath = (String) FastDFSUtil.upload(xmlFile, xmlFis, baos);

                /*
                * 信息入库
                * */
                tileInfosDO.setStoragePath((String) FastDFSUtil.upload(imgFile, imgFis, baos));
                tileInfosDO.setVisualPath((String) FastDFSUtil.upload(visualFile, visualFis, baos));
                tileInfosDO.setLabelPath((String) FastDFSUtil.upload(xmlFile, xmlFis, baos));
                tileInfosDO.setImagesetid(35);
                tileInfosDO.setDataPath(imgName);
                //态势信息入库(新增)
                tileInfosMapper.insertTilesInfo(tileInfosDO);
                fileFileCount++;
                while (len % 1000 == 0){
                    sqlSession.commit();
                }
            }
            baos.close();
            imgFis.close();
            visualFis.close();
            xmlFis.close();
            //图像信息更新(number)
            imageDataSetInfoDO.setNumber(fileFileCount);
            imageDatasetMapper.updateNumber(imageDataSetInfoDO);
        }

        return new CommonResponseDTO().success().message("文件上传成功");
    }

}