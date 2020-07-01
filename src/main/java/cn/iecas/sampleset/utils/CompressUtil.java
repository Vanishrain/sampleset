package cn.iecas.sampleset.utils;

import cn.iecas.sampleset.common.constant.CompressCmd;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 解压缩工具类
 */
@Slf4j
public class CompressUtil {
    private final static byte[] RAR_HEADER = {82,97,114,33}; //RAR格式的二进制头

    private final static byte[] ZIP_HEADER = {80,75,3,4}; //

    private final static byte[] ZIP_OLD_HEADER = {80,75,3,4};

    private final static byte[] TAR_HEADER = {117,115,116,97,114};

    /**
     * 判断文件的压缩格式
     * @param filePath
     * @return
     */
    private static String getCompressFileType(String filePath){
        byte[] fileHeader = new byte[4];
        byte[] tarFileHeader = new byte[5];
        RandomAccessFile randomAccessFile = null;
        try{
            randomAccessFile = new RandomAccessFile(filePath,"r");
            randomAccessFile.read(fileHeader);
            randomAccessFile.seek(256);
            randomAccessFile.read(tarFileHeader);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                assert randomAccessFile != null;
                randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if (Arrays.equals(fileHeader,RAR_HEADER))
            return CompressCmd.RAR_POSTFIX;

        if (Arrays.equals(fileHeader,ZIP_HEADER) || Arrays.equals(fileHeader, ZIP_OLD_HEADER))
            return CompressCmd.ZIP_POSTFIX;

        if (Arrays.equals(tarFileHeader,TAR_HEADER))
            return CompressCmd.TAR_POSTFIX;

        return null;
    }

    /**
     * 根据文件类型获取列表命令
     * @param fileType
     * @return
     */
    private static String getListCmd(String fileType){
        String listCmd = null;
        switch (fileType) {
            case CompressCmd.RAR_POSTFIX:
                listCmd = CompressCmd.RAR_LIST_CMD;
                break;
            case CompressCmd.TAR_POSTFIX:
                listCmd = CompressCmd.TAR_LIST_CMD;
                break;
            default:
                listCmd = CompressCmd.ZIP_LIST_CMD;
        }

        return listCmd;
    }

    /**
     * 根据文件类型获取相应的解压缩命令
     * @param fileType
     * @return
     */
    private static String getDecompressCmd(String fileType, String srcPath, String desPath){
        String decompressCmd = null;
        switch (fileType) {
            case CompressCmd.RAR_POSTFIX:
                decompressCmd = String.format("%s %s %s",CompressCmd.RAR_DECOMPRESS_CMD, srcPath, desPath);
                break;
            case CompressCmd.TAR_POSTFIX:
                decompressCmd = String.format("%s %s %s",CompressCmd.TAR_DECOMPRESS_CMD, srcPath, desPath);
                break;
            default:
                decompressCmd = String.format("%s %s -d %s",CompressCmd.ZIP_DECOMPRESS_CMD, srcPath, desPath);
        }

        return decompressCmd;
    }


    /**
     * 压缩为zip格式
     * @param srcPath
     * @param desPath
     */
    public static void compress(String srcPath, String desPath){
        File srcFile = new File(srcPath);
        String compressCmd = String.format("zip %s -r %s", desPath,srcFile.getName());
        try {
            Process process = Runtime.getRuntime().exec(compressCmd,null,srcFile.getParentFile()); //执行解压命令
            new RunThread(process.getInputStream(), "INFO").start();
            new RunThread(process.getErrorStream(), "ERROR").start();

            log.info("正在压缩文件:{}", srcPath);
            process.waitFor();  //等待压缩结束
            log.info("文件:{} 压缩完成", srcPath);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            log.info("压缩文件:{} 失败", srcFile.getName());
        }
    }


    /**
     * 从压缩包中删除文件
     * @param srcPath
     * @param fileName
     */
    public static void delete(String srcPath, String fileName){
        String cmd = String.format("zip %s -d %s",srcPath,fileName);

        try {
            Process process = Runtime.getRuntime().exec(cmd); //执行解压命令
            new RunThread(process.getInputStream(), "INFO").start();
            new RunThread(process.getErrorStream(), "ERROR").start();

            log.info("正在从压缩包{} 删除文件:{}",srcPath,fileName);
            process.waitFor();  //等待解压结束
            log.info("从压缩包{} 删除文件:{}成功",srcPath,fileName);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            log.info("从压缩包{} 删除文件:{}失败",srcPath,fileName);
        }
    }


    /*
    * 解压
    * */
    public static String decompress(String srcPath,   //待解压文件路径
                                          String destPath) throws Exception //文件后缀？
    {
        File srcFile = new File(srcPath);//压缩包
        String fileType = getCompressFileType(srcPath);
        if (fileType == null)
            throw new Exception("文件：" + srcFile.getName() + " 不是压缩格式");

        if (destPath == null)
            destPath = srcFile.getParentFile().getAbsolutePath();

        File destFile = new File(destPath);
        if (!destFile.exists())
            destFile.mkdirs();

        String decompressCmd = getDecompressCmd(fileType,srcPath,destPath);

        try {
            Process process = Runtime.getRuntime().exec(decompressCmd); //执行解压命令
            new RunThread(process.getInputStream(), "INFO").start();
            new RunThread(process.getErrorStream(), "ERROR").start();

            log.info("正在解压缩文件:{}", srcPath);
            process.waitFor();  //等待解压结束
            log.info("文件:{} 解压缩完成", srcPath);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            log.info("解压缩文件:{} 失败", srcFile.getName());
        }
        return destPath;
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
     * 获取压缩文件内某一文件夹的内容,并且返回需要解压的文件名
     * @param srcPath
     * @return
     */
    public static List<String> getZipFileNameList(String srcPath, String dir){
        String listCmd = CompressCmd.ZIP_LIST_CMD;
        if (null != dir){
            String dirPath = String.format("%s %s%s*.* | awk {print $4}",srcPath,dir,File.separator);
            listCmd += " " + dirPath;
        }


        List<String> resultList = new ArrayList<>();

        try {
            log.info("正在获取文件:{} 列表", srcPath);
            Process process = Runtime.getRuntime().exec(listCmd);
            InputStream in = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String resultLine = null;

            while (true) {
                resultLine = bufferedReader.readLine();
                if (null == resultLine)
                    break;

                if (resultLine.contains(dir+"/")){
                    resultLine = resultLine.trim().split(" ")[6].substring(dir.length()+1);
                    resultList.add(resultLine);
                }

            }

            log.info("文件:{} 中文件夹{} 列表获取完毕", srcPath,dir);
        }catch (IOException e) {
            e.printStackTrace();
            log.info("文件：{}中文件夹{} 列表获取失败，命令执行出错", srcPath,dir);
        }
        return  resultList;
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