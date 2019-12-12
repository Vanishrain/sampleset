package cn.iecas.datasets.image.service.impl;

import cn.iecas.datasets.image.common.domain.QueryRequest;
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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.csource.fastdfs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class StorageServiceImpl implements StorageService {

    private final Logger logger = LoggerFactory.getLogger(StorageServiceImpl.class);

    @Value("${value.dir.monitorDir}")
    private Path rootPath;  // 本地保存文件的目录

    @Value("${value.dir.decompressImgDir}")
    private Path decompressRootDir; //解压目录

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
    private String downloadPath;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private TileInfosMapper tileInfosMapper;
    @Autowired
    private ImageDatasetMapper imageDatasetMapper;

    @Autowired
    public StorageServiceImpl(@Value("${value.dir.monitorDir}") String location) { //获取文件上传路径
        this.rootPath = Paths.get(location);
    }

    /*
    * 上传文件
    * */
    @Override
    public Object uploadFileByMappedByteBuffer(MultipartFileParam param) throws IOException {
        String fileId = "";
        String fileName = param.getName();
        String uploadDirPath = rootPath + "\\" + param.getMd5();   //上传文件所在目录
        String decompressDirPath = decompressRootDir.toString();  //解压文件所在路径
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
             * 批量
             * */
            File file = null;
            String tileFilePath = decompressDirPath + "\\";
            TileInfosDO tileInfosDO = new TileInfosDO();
            ImageDataSetInfoDO imageDataSetInfoDO = imageDatasetMapper.getImageDataSetById(35);
            int fileFileCount = imageDataSetInfoDO.getNumber();
            /*
            * FastDFS上传并入库
            * */
            for (String tileFile : decompressedFile){
                file = new File(tileFilePath + tileFile);
                fileId = (String) FastDFSUtil.upload(file); //文件所在服务器的路径

                tileInfosDO.setDataPath(StringUtils.substringAfter(tileFile, "\\"));  //文件名
                tileInfosDO.setImagesetid(35);
                tileInfosDO.setStoragePath(fileId); //存储路径
                //态势信息入库(新增)
                tileInfosMapper.insertTilesInfo(tileInfosDO);
                //图像信息更新(number)
                imageDataSetInfoDO.setNumber(fileFileCount++);
                imageDatasetMapper.updateNumber(imageDataSetInfoDO);
            }
        }

        return new CommonResponseDTO().success().message("文件上传成功");
    }

    /*
    * 下载
    * */
    @Override
    public void download(TileInfosDO tileInfosDO) {
        String targetFilePath = tileInfosMapper.getStoragePath(tileInfosDO);

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

    /*
    * 按名称查询
    * */
    public String getByName(TileInfosDO tileInfosDO) throws IOException {
        String storagePath = tileInfosMapper.getStoragePath(tileInfosDO);
        String urlString = "http://" + fastdfsServer + "/" + storagePath;

        URL url = new URL(urlString);   //构造URL
        URLConnection urlConnection = url.openConnection(); //打开连接
        InputStream in = urlConnection.getInputStream();
        byte[] imageData = null;
        try {
            imageData = new byte[in.available()];
            in.read(imageData);
            in.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        BASE64Encoder encoder = new BASE64Encoder();
        String imageDataString = encoder.encode(imageData);

        return "data:image/jpeg;base64," + imageDataString;
    }

    /*
    * 查询所有
    * */
    public List<String> getAll(QueryRequest request) throws IOException {
        List<String> result = new ArrayList<>();
        String urlStringPrefix = "http://" + fastdfsServer + "/";
        String urlString;
        Page<String> page = new Page<>();
        page.setCurrent(request.getPageNo());
        page.setSize(request.getPageSize());

        IPage<String> storagePaths = tileInfosMapper.getAll(page,request);

        URL url;
        URLConnection urlConnection;
        byte[] imageData;
        InputStream in = null;
        BASE64Encoder encoder = new BASE64Encoder();

        for (int i=0; i<storagePaths.getSize(); i++){
            urlString = urlStringPrefix + storagePaths.getRecords().get(i);
            url = new URL(urlString);
            urlConnection = url.openConnection();
            in = urlConnection.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                int length = 0;
                imageData = new byte[1024];
                while((length = in.read(imageData)) != -1){ //读取文件
                    byteArrayOutputStream.write(imageData,0,length);
                }
                imageData = byteArrayOutputStream.toByteArray();
                String encodedimageData = encoder.encode(imageData);
                String imageBase64String = "data:image/jpeg;base64," + encodedimageData;
                result.add(imageBase64String);

            } catch (Exception e){
                e.printStackTrace();
            }
        }
        in.close();

        return result;
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
            // decomporessCmd=String.format("%s %s %s",this.jobInfo.getDecompressRarCmd(),srcPath,desPath);

            return  decompressRarCmd;
        if (FilenameUtils.isExtension(fileName,"zip"))
            //comporessCmd =String.format("%s  %s  -d %s",this.jobInfo.getDecompressZipCmd(),srcPath,desPath);

            return decompressZipCmd;

        //comporessCmd=String.format("%s %s -C %s",this.jobInfo.getDecompressTarCmd(),srcPath,desPath);
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

}