package cn.iecas.datasets.image.utils;

import cn.iecas.datasets.image.pojo.dto.CommonResponseDTO;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

import static org.csource.fastdfs.ClientGlobal.init;

public class FastDFSUtil {
    private static String conf_filename = "src/main/resources/fdfs_client.conf";
    private static TrackerServer trackerServer = null;
    private static StorageServer storageServer = null;

    static {
        try {
            init(conf_filename);   //根据配置文件初始化
        } catch (IOException | MyException e) {
            e.printStackTrace();
        }
    }

    public static StorageClient1 getSrorageClient(){
        TrackerClient trackerClient = new TrackerClient();
        StorageClient1 storageClient = null;
        try {
            trackerServer = trackerClient.getConnection();    //跟踪服务器
            storageServer = trackerClient.getStoreStorage(trackerServer); //存储服务器

            String storageIP = storageServer.getSocket().getInetAddress().getHostAddress(); //IP
            int port = storageServer.getSocket().getPort(); //端口
            storageClient = new StorageClient1(trackerServer, new StorageServer(storageIP, port, 0));
        } catch (IOException e) {
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
    public static Object upload(File file, InputStream fis, ByteArrayOutputStream baos) {
        try {
            if (file == null){
                return new CommonResponseDTO().fail().message("上传文件为空");
            }else {
                String tempFileName = file.getName();

                int len;
                byte[] buffer = new byte[1024];
                while ((len = fis.read(buffer)) != -1){
                    baos.write(buffer, 0, len);
                }
                byte[] fileBuff = baos.toByteArray();

                //FastDFS方式
                String fileExtName = tempFileName.substring(tempFileName.lastIndexOf(".")); //后缀

                String fileId = getSrorageClient().upload_file1(fileBuff, fileExtName, null);
                System.out.println("上传至服务的文件ID为：" + fileId);

                return fileId;  //上传成功返回文件ID
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "文件上传失败 " + e.getMessage();
        } finally {
            try {
                closeConnection();
                baos.close();
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
