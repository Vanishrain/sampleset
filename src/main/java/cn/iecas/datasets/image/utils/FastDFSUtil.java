package cn.iecas.datasets.image.utils;

import org.csource.common.MyException;
import org.csource.fastdfs.*;

import java.io.*;
public class FastDFSUtil {
    private static String conf_filename = "src/main/resources/fdfs_client.conf";
    private static TrackerServer trackerServer = null;
    private static StorageServer storageServer = null;

    static {
        try {
            ClientGlobal.init(conf_filename);   //根据配置文件初始化
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
            StorageClient1 storageClient = new StorageClient1(trackerServer, storageServer);

            return storageClient.download_file1(fileId);
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
        TrackerClient trackerClient = new TrackerClient();
        StorageClient1 storageClient = null;
        try {
            trackerServer = trackerClient.getConnection();    //跟踪服务器
            storageServer = trackerClient.getStoreStorage(trackerServer); //存储服务器
            if (storageServer == null){
                throw new Exception("获取FastDFS连接失败");
            }

            String storageIP = storageServer.getSocket().getInetAddress().getHostAddress(); //IP
            int port = storageServer.getSocket().getPort(); //端口
            storageClient = new StorageClient1(trackerServer, new StorageServer(storageIP, port, 0));
        } catch (IOException e) {
            e.printStackTrace();
    } catch (Exception e) {
            e.printStackTrace();
        }

        return storageClient;
    }

    /*
    * 关闭FastDFS连接
    * */
    public static void closeConnection(){
        try {
            if (storageServer != null){
                storageServer.close();
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

            String fileId = getSrorageClient().upload_file1(fileBuff, fileExtName, null);
            System.out.println("上传至服务的文件ID为：" + fileId);
            return fileId;  //上传成功返回文件ID
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
