package cn.iecas.datasets.image.utils;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 解压缩工具类
 */
public class CompressUtil {
    private static Logger logger = LoggerFactory.getLogger(CompressUtil.class);

    /*
    * 解压
    * */
    public static List<String> decompress(String srcPath,   //待解压文件路径
                                          String destPath,   //解压目标路径
                                          String decompressCmdPrefix,   //解压命令前缀？
                                          String decompressCmdListPrefix,   //解压命令列表前缀？
                                          List<String> filePostfix) //文件后缀？
    {
        File srcFile = new File(srcPath);   //压缩包

        //获取查询压缩文件内容的命令
        String decompressCmdList = getDecompressCmdList(srcPath, decompressCmdListPrefix);

        //获取压缩文件内的内容,并且返回需要解压的 tif 和 tiff 类型的文件名
        List<String> resultList = getFileNameList(srcPath, decompressCmdList, filePostfix);
        String decompressCmd = getDecompressCmd(srcPath, destPath, decompressCmdPrefix);

        try {
            Process process = Runtime.getRuntime().exec(decompressCmd); //执行解压命令
            new RunThread(process.getInputStream(), "INFO").start();
            new RunThread(process.getErrorStream(), "ERROR").start();

            logger.info("正在解压缩文件:{}", srcPath);
            process.waitFor();  //等待解压结束
            logger.info("文件:{} 解压缩完成", srcPath);

//            process = Runtime.getRuntime().exec(decompressCmdList);
//            process.waitFor();
//            InputStream in = process.getInputStream();
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
//            String resultLine = null;
//            logger.info("正在获取文件:{} 列表",srcPath);
//            while(true){
//                resultLine = bufferedReader.readLine();
//                if (resultLine==null)
//                    break;
//                if (FilenameUtils.isExtension(resultLine,filePostfix)) {
//                    resultList.add(resultLine);
//                }
//            }
//            System.out.println(resultList.toString());
//            logger.info("文件:{} 列表获取完毕",srcPath);

        } catch (IOException e) {
            e.printStackTrace();
            logger.info("解压缩文件:{} 失败", srcFile.getName());

            return resultList;

        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.info("解压缩文件:{} 失败", srcFile.getName());

            return resultList;
        }

        return resultList;
    }

    /**
     * 获取查询压缩文件内容的命令
     * @param srcPath
     * @param decompressCmdListPrefix
     * @return
     */
    public static String getDecompressCmdList(String srcPath, String decompressCmdListPrefix){
        String decompressCmdList = String.format("%s %s", decompressCmdListPrefix,srcPath);

        return  decompressCmdList;
    }
    /**
     * 获取压缩文件内的内容,并且返回需要解压的tif和tiff类型的文件名
     * @param decompressCmdList
     * @param filePostfix
     * @return
     */
    public  static  List<String>  getFileNameList(String srcPath,
                                                  String decompressCmdList,
                                                  List<String> filePostfix){
        List<String> resultList = new ArrayList<>();

        try {
            logger.info("正在获取文件:{} 列表", srcPath);

            Process process = Runtime.getRuntime().exec(decompressCmdList);
//            new RunThread(process.getInputStream(), "INFO").start();
//            new RunThread(process.getErrorStream(), "ERROR").start();

//            process.waitFor();
            InputStream in = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String resultLine = null;

            while (true) {
                resultLine = bufferedReader.readLine();

                if (resultLine == null)
                    break;
                if (FilenameUtils.isExtension(resultLine, filePostfix)) {
                    if(FilenameUtils.isExtension(srcPath,"zip")){
                        resultLine=resultLine.substring(resultLine.lastIndexOf(" ")+1);
                    }

                    resultList.add(resultLine);
                }
            }

            logger.info("文件:{} 列表获取完毕", srcPath);
        }catch (IOException e) {
            e.printStackTrace();
            logger.info("文件：{}列表获取失败，命令执行出错", srcPath);
        }
        /*catch (InterruptedException e){
            e.printStackTrace();
            logger.info("文件列表：{}获取失败，遇到异常中断",srcPath);
        }*/

        return  resultList;
    }

    /*
    * 根据压缩包类型返回要执行的解压命令
    * */
    public static String getDecompressCmd(String srcPath,
                                          String destPath,
                                          String decompressCmdPrefix){
        String decomporessCmd;

        if(FilenameUtils.isExtension(srcPath,"rar") ) {
            decomporessCmd = String.format("%s %s %s", decompressCmdPrefix, srcPath, destPath);

            return  decomporessCmd;
        }
        if (FilenameUtils.isExtension(srcPath,"zip")) {
            decomporessCmd = String.format("%s  %s   -d %s",decompressCmdPrefix, srcPath, destPath);
            return  decomporessCmd;
        }
        if(FilenameUtils.isExtension(srcPath,"tar") ){
            decomporessCmd = String.format("%s  %s  %s",decompressCmdPrefix , destPath, srcPath);
            return  decomporessCmd;
        }

        return  null;
    }

    /**
     * 根据后缀名判断是否为压缩文件
     *
     * @param fileName
     * @return
     */
    public static boolean isCompressedFile(String fileName) {
        if (fileName.endsWith(".rar") || fileName.endsWith(".zip"))
            return true;

        return false;
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