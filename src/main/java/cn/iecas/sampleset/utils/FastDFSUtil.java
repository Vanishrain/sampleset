package cn.iecas.sampleset.utils;

import lombok.extern.slf4j.Slf4j;
import org.csource.common.MyException;
import org.csource.fastdfs.*;

import java.io.*;

@Slf4j
public class FastDFSUtil {
    private static String conf_filename = "src/main/resources/fdfs_client.conf";
    private static TrackerServer trackerServer = null;
    private static StorageClient1 storageClient1 = null;

    static {
        try {
            String userDir = System.getProperty("user.dir");
            File file = new File(userDir + File.separator + "fdfs_client.conf");
            if (file.exists()){
                ClientGlobal.init(file.getAbsolutePath());
                log.info("fastdfs从: {} 获取配置文件",file.getAbsolutePath());
            }
            else{
                ClientGlobal.init(conf_filename);   //根据配置文件初始化
                log.info("fastdfs从: {} 获取配置文件",conf_filename);
            }
        } catch (IOException | MyException e) {
            e.printStackTrace();
        }
    }

    /*
    * 下载
    * */
    public static byte[] download(String fileId) throws Exception {
        TrackerServer trackerServer = null;
        StorageServer storageServer = null;
        try {
            TrackerClient trackerClient = new TrackerClient();
            trackerServer = trackerClient.getConnection();
            StorageClient1 storageClient1 = new StorageClient1(trackerServer, storageServer);
            return storageClient1.download_file1(fileId);
        } catch (Exception e) {
            throw new Exception("下载失败");
        } finally {
            try {
                if (null != storageServer){
                    storageServer.close();
                }
                if (null != trackerServer){
                    trackerServer.close();
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /*
    * 删除
    * 成功返回0
    * 非0则操作失败
    * */
    public static int delete(String fileId){
        int result = 1;
        StorageClient1 storageClient1 = getSrorageClient();
        try {
            result = storageClient1.delete_file1(fileId);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return result;
        } catch (MyException e) {
            e.printStackTrace();
            return result;
        }finally {
            closeConnection();
        }
    }

    /*
    * 获得StorageClient连接
    * */
    public static StorageClient1 getSrorageClient(){
        if (storageClient1 == null){
            TrackerClient trackerClient = new TrackerClient();
            try {
                trackerServer = trackerClient.getConnection();    //跟踪服务器
                storageClient1 = new StorageClient1(trackerServer,null);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return storageClient1;
    }

    /*
    * 关闭FastDFS连接
    * */
    public static void closeConnection(){
        try {
            if (storageClient1 != null){
                storageClient1.close();
            }
            if (trackerServer != null){
                trackerServer.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /*
    * 上传
    * */
    public static String upload(File file) {
        FileInputStream fileInputStream = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            if (null == file || !file.exists())
                throw new Exception("上传文件为空");
            fileInputStream = new FileInputStream(file);
            String tempFileName = file.getName();
            int len;
            byte[] buffer = new byte[1024];
            while ((len = fileInputStream.read(buffer)) != -1){
                baos.write(buffer, 0, len);
            }
            byte[] fileBuff = baos.toByteArray();

            String fileExtName = tempFileName.substring(tempFileName.lastIndexOf(".")); //后缀
            try{
                String fileId = getSrorageClient().upload_file1(fileBuff, fileExtName, null);
                log.info("上传至服务的文件ID为：{}" ,fileId);
                return fileId;  //上传成功返回文件ID
            }catch (Exception e){
                System.out.println("df");
            }
            return "test";
        } catch (Exception e) {
            e.printStackTrace();
            return "文件上传失败 " + e.getMessage();
        } finally {
            try {
                closeConnection();
                baos.close();
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
