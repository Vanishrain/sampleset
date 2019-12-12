package cn.iecas.datasets.image.utils;

import cn.iecas.datasets.image.pojo.dto.CommonResponseDTO;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class FastDFSUtil {
    private static String conf_filename = "src/main/resources/fdfs_client.conf";
    private static TrackerServer trackerServer = null;
    private static TrackerClient trackerClient = null;
    private static StorageServer storageServer = null;

    /*
    * 获取FastDFS连接
    * */
    public static StorageClient1 getConnection(){
        try {
            ClientGlobal.init(conf_filename);
            trackerServer = trackerClient.getConnection();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        StorageClient1 storageClient1 = new StorageClient1(trackerServer, storageServer);
        return storageClient1;
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
    public static Object upload(File file) {
        try {
            if (file == null){
                return new CommonResponseDTO().fail().message("上传文件为空");
            }else {
                String tempFileName = file.getName();

                FileInputStream fis = new FileInputStream(file);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int len;
                byte[] buffer = new byte[1024];
                while ((len = fis.read(buffer)) != -1){
                    baos.write(buffer, 0, len);
                }
                byte[] fileBuff = baos.toByteArray();

                //FastDFS方式
                ClientGlobal.init(conf_filename);   //根据配置文件初始化
                String fileExtName = tempFileName.substring(tempFileName.lastIndexOf(".")); //后缀

                /*
                * 建立连接
                * */
                TrackerClient trackerClient = new TrackerClient();
                TrackerServer trackerServer = trackerClient.getConnection();    //跟踪服务器
                StorageServer storageServer = trackerClient.getStoreStorage(trackerServer); //存储服务器

                String storageIP = storageServer.getSocket().getInetAddress().getHostAddress(); //IP
                int port = storageServer.getSocket().getPort(); //端口
                StorageServer storageServer2 = new StorageServer(storageIP, port, 0);
                StorageClient1 storageClient2 = new StorageClient1(trackerServer, storageServer2);

                /*
                * 设置元信息
                * */
                NameValuePair[] metaList = new NameValuePair[3];
                metaList[0] = new NameValuePair("fileName", tempFileName);  //文件名
                metaList[1] = new NameValuePair("fileExtName", fileExtName);    //后缀
                metaList[2] =new NameValuePair("fileLength", String.valueOf(file.length()));

                String fileId = storageClient2.upload_file1(fileBuff, fileExtName, metaList);
                System.out.println("上传至服务的文件ID为：" + fileId);
                //TODO  追加业务代码

                baos.close();
                fis.close();

                return fileId;  //上传成功返回文件ID
            }
        } catch (Exception e){
            e.printStackTrace();

            System.out.println(e.getMessage());
            return "文件上传失败 " + e.getMessage();
        }
    }

}
