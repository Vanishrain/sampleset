package cn.iecas.datasets.image.service.impl;

import cn.iecas.datasets.image.dao.ImageDatasetMapper;
import cn.iecas.datasets.image.dao.TileInfosMapper;
import cn.iecas.datasets.image.datasource.BaseDataSource;
import cn.iecas.datasets.image.pojo.domain.ImageDataSetInfoDO;
import cn.iecas.datasets.image.pojo.domain.TileInfosDO;
import cn.iecas.datasets.image.pojo.entity.uploadFile.MultipartFileParam;
import cn.iecas.datasets.image.service.StorageService;
import cn.iecas.datasets.image.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class StorageServiceImpl implements StorageService {

    private final Logger logger = LoggerFactory.getLogger(StorageServiceImpl.class);

    @Value("${value.dir.monitorDir}")
    private Path rootPath;  // 本地暂时保存文件的目录

    @Value("${value.downloadPath}")
    private String downloadDir;

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
    BaseDataSource baseDataSource;

    @Autowired
    public StorageServiceImpl(@Value("${value.dir.monitorDir}") String location) { //获取文件上传路径
        this.rootPath = Paths.get(location);
    }

    /*
    * 上传文件
    * */
    @Override
    public String uploadTiles(MultipartFileParam param, HttpServletRequest request) throws Exception {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        String uploadResult = "fail";

        if (isMultipart) {
            try {
                if (0 == param.getImagesetid()){
                    throw new Exception("该数据集不存在，请先添加数据集!");
                }
                uploadResult = uploadFileByMappedByteBuffer(param); //上传过程
            } catch (IOException e) {
                e.printStackTrace();
                log.error("文件上传失败! {}", param.toString());
            }
        } else {
            throw new Exception("请选择文件上传");
        }

        return uploadResult;
    }

    /*
    * 下载
    * */
    @Override
    public void download(int imagesetid) throws Exception {
        File downloadDir = new File(downloadPath);  //下载根目录
        if (!downloadDir.exists()){
            downloadDir.mkdirs();
        }
        File imgDir =  new File(downloadPath + File.separator  + "imgs");
        File visualDir =  new File(downloadPath + File.separator + "visual");
        File xmlsDir =  new File(downloadPath + File.separator + "xmls");
        if (!imgDir.exists() || !visualDir.exists() || !xmlsDir.exists()){
            imgDir.mkdirs();
            visualDir.mkdirs();
            xmlsDir.mkdirs();
        }

        /*
        * 查询所有分片
        * 遍历获取分片的存储路径
        * */
        List<TileInfosDO> tileInfosDOs = tileInfosMapper.getAllTileById(imagesetid);
        if (tileInfosDOs == null || tileInfosDOs.size() == 0){
            throw new Exception("下载失败！该数据集无相应切片");
        }
        String compressFileName = imageDatasetMapper.getImageDataSetById(imagesetid).getName() + ".rar"; //数据集名称
        String imgStoragePath;
        String visualStoragePath;
        String xmlStoragePath;
        OutputStream imgOS;
        OutputStream visualOS;
        OutputStream xmlOS;

        for (TileInfosDO tileInfosDO : tileInfosDOs){
            String commonFileName = tileInfosDO.getDataPath();
            String suffix = commonFileName.substring(commonFileName.indexOf("."));//后缀
            String fileName = commonFileName.substring(0, commonFileName.lastIndexOf("."));
            imgOS = new FileOutputStream(imgDir + File.separator + fileName + suffix);
            visualOS = new FileOutputStream(visualDir + File.separator + fileName + suffix);
            xmlOS = new FileOutputStream(xmlsDir + File.separator + fileName + ".xml");

            imgStoragePath = tileInfosDO.getStoragePath();
            visualStoragePath = tileInfosDO.getVisualPath();
            xmlStoragePath = tileInfosDO.getLabelPath();

            byte[] imgByteArray = baseDataSource.download(imgStoragePath);
            byte[] visualByteArray = baseDataSource.download(visualStoragePath);
            byte[] xmlByteArray = baseDataSource.download(xmlStoragePath);
            if (imgByteArray != null){
                imgOS.write(imgByteArray);
                imgOS.close();
            }if (visualByteArray != null){
                visualOS.write(visualByteArray);
                visualOS.close();
            }if (xmlByteArray != null){
                xmlOS.write(xmlByteArray);
                xmlOS.close();
            }
        }
        if (imgDir.listFiles()==null && visualDir.listFiles()==null && xmlsDir.listFiles()==null){
            try {
                throw new Exception("下载失败，该数据集无对应切片");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /*
        下载完成后删除某一空文件夹
        * 删除空文件夹
        * */
        if (imgDir.listFiles() == null){
            deleteFile(imgDir);
        }
        if (visualDir.listFiles() == null){
            deleteFile(visualDir);
        }
        if (xmlsDir.listFiles() == null){
            deleteFile(xmlsDir);
        }

        /*
        * 压缩
        * */
        String compressTargetFile = downloadDir + File.separator + compressFileName;
        String compressCommand = "rar m -r -ep1 " + compressTargetFile + " " + downloadDir + "\\*";
        Process process = Runtime.getRuntime().exec(compressCommand);
        new RunThread(process.getInputStream(), "INFO").start();
        new RunThread(process.getErrorStream(), "ERROR").start();
        process.waitFor();
        System.out.println("文件压缩完成");

        /*
        * 返回下载流
        * */
        File compressedFile = new File(downloadDir + File.separator + compressFileName);
        InputStream in = new BufferedInputStream(new FileInputStream(compressedFile));
        byte[] buffer = new byte[in.available()];
        in.read(buffer);
        in.close();
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = servletRequestAttributes.getResponse();
        response.reset();   //清空response
        response.setContentType("application/octet-stream;charset=UTF-8");
        String fileName = new String(compressFileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        response.setHeader("Content-disposition", "attachment;filename=" + java.net.URLEncoder.encode(compressFileName,"UTF-8"));
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        compressedFile.delete();
        OutputStream out = response.getOutputStream();
        out.write(buffer);
        out.flush();
        out.close();
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
    private String uploadFileByMappedByteBuffer(MultipartFileParam param) throws IOException {
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
             * 上传至文件系统
             * */
            TileInfosDO tileInfosDO = new TileInfosDO();
            ImageDataSetInfoDO imageDataSetInfoDO = imageDatasetMapper.getImageDataSetById(param.getImagesetid());
            if (imageDataSetInfoDO == null){
                return "上传失败，请选择待上传切片的数据集！";
            }
            long fileFileCount = imageDataSetInfoDO.getNumber(); //上传文件数量
            FileInputStream imgFis = null;
            FileInputStream visualFis = null;
            FileInputStream xmlFis = null;

            String tileFileRootDirPath = decompressDirPath + "\\car";
            String imgsDir = tileFileRootDirPath + File.separator + "imgs";
            String visualDir = tileFileRootDirPath + File.separator + "visual";
            String xmlsDir = tileFileRootDirPath + File.separator + "xmls";

            File[] imgs = new File(imgsDir).listFiles();
            int len = imgs.length;
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            for (int i=0; i<len; i++){
                File imgFile = imgs[i]; //img
                String imgName = imgFile.getName();
                String commonName = imgName.substring(0, imgName.lastIndexOf("."));
                String suffix = imgName.substring(imgName.indexOf("."));

                File visualFile = new File(visualDir + File.separator + commonName + suffix);
                File xmlFile = new File(xmlsDir + File.separator + commonName + ".xml");

                /*
                * 上传文件
                * 信息入库
                * */
                imgFis = new FileInputStream(imgFile);
                tileInfosDO.setStoragePath(FastDFSUtil.upload(imgFile, imgFis));
                if (visualFile.exists()){
                    visualFis = new FileInputStream(visualFile);
                    tileInfosDO.setVisualPath(FastDFSUtil.upload(visualFile, visualFis));
                }
                if (xmlFile.exists()){
                    xmlFis = new FileInputStream(xmlFile);
                    tileInfosDO.setLabelPath(FastDFSUtil.upload(xmlFile, xmlFis));
                }
                tileInfosDO.setImagesetid(param.getImagesetid());
                tileInfosDO.setDataPath(imgName);
                tileInfosDO.setCreateTime();
                tileInfosMapper.insertTilesInfo(tileInfosDO);   //态势信息入库(新增)

                fileFileCount++;
                while (len % 1000 == 0){
                    sqlSession.commit();
                }
            }
            imgFis.close();
            visualFis.close();
            xmlFis.close();
            imageDataSetInfoDO.setNumber(fileFileCount);
            imageDatasetMapper.updateNumber(imageDataSetInfoDO);

            return "success";
        }

        return "fail";
    }

    private boolean deleteFile(File file){
        if (!file.exists()){
            return false;
        }
        if (file.isDirectory()){
            File[] files = file.listFiles();
            for (File file1 : files){
                deleteFile(file1);
            }
        }
        return file.delete();
    }
}

class RunThread extends Thread{
    InputStream is;
    String type;

    RunThread(InputStream is, String type){
        this.is = is;
        this.type = type;
    }

    @Override
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null){
                System.out.println(type + ">" + line);
            }
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
    }
}