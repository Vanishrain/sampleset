package cn.iecas.sampleset.utils;

import cn.iecas.sampleset.pojo.dto.Manifest;
import com.alibaba.fastjson.JSONObject;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.Dom4JDriver;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * 文件md5值
 */
@Slf4j
public class FileUtils {

    /**
     * 封装paths方法，使任意类型均可转换为string
     * @param first 第一个值
     * @param more 其它值
     * @return
     */
    public static Path getPath(Object first, Object... more){
        String[] strMore = Arrays.stream(more).map(String::valueOf).toArray(String[]::new);
        return Paths.get(String.valueOf(first),strMore);
    }

    /**
     * 封装paths方法，使任意类型均可转换为string
     * @param first 第一个值
     * @param more 其它值
     * @return
     */
    public static String getStringPath(Object first, Object... more){
        String[] strMore = Arrays.stream(more).map(String::valueOf).toArray(String[]::new);
        return Paths.get(String.valueOf(first),strMore).toString();
    }

    /**
     * 封装paths方法，使任意类型均可转换为string
     * @param first 第一个值
     * @param more 其它值
     * @return
     */
    public static File getFile(Object first, Object... more){
        String[] strMore = Arrays.stream(more).map(String::valueOf).toArray(String[]::new);
        return Paths.get(String.valueOf(first),strMore).toFile();
    }

    /**
     * 读取文件
     * @param filePath 文件路径
     * @return 文件内容
     */
    public static String readFile(String filePath){
        StringBuilder content = new StringBuilder();
        try(RandomAccessFile randomAccessFile = new RandomAccessFile(filePath,"r")){
            String line;
            while((line = randomAccessFile.readLine())!=null)
                content.append(new String(line.getBytes("ISO-8859-1"),"utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    /**
     * 拷贝文件
     * @param srcFile
     * @param destFile
     * @throws IOException
     */
    public static void copyFile(File srcFile, File destFile) throws IOException {
        org.apache.commons.io.FileUtils.copyFile(srcFile,destFile);
    }

    /**
     * 拷贝文件到某一文件夹
     * @param srcFile
     * @param destDir
     * @throws IOException
     */
    public static void copyFileToDirectory(File srcFile, File destDir) throws IOException {
        org.apache.commons.io.FileUtils.copyFileToDirectory(srcFile,destDir);
    }

    /**
     * 将java对象保存为xml文件
     * @param clazz 对象类型
     * @param data 对象
     * @param destFile 输出文件
     * @throws IOException
     */
    public static void objectToXML(Class clazz, Object data, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        FileOutputStream fileOutputStream = new FileOutputStream(destFile);
        XStream xStream = new XStream(new Dom4JDriver());
        xStream.processAnnotations(clazz);
        xStream.toXML(data,fileOutputStream);
        fileOutputStream.close();
    }
}
