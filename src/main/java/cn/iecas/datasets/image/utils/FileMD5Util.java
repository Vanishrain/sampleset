package cn.iecas.datasets.image.utils;

import cn.iecas.datasets.image.pojo.dto.CommonResponseDTO;
import cn.iecas.datasets.image.pojo.entity.uploadFile.ResultStatus;
import cn.iecas.datasets.image.pojo.entity.uploadFile.ResultVo;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.PrivilegedAction;
import java.util.LinkedList;
import java.util.List;

/**
 * 文件md5值
 */
@Component
public class FileMD5Util {
    private final static Logger logger = LoggerFactory.getLogger(FileMD5Util.class);
    @Autowired
    private StringRedisTemplate stringRedisTemplate1;
    private static StringRedisTemplate stringRedisTemplate;

    /*
    * 通过MD5的值判断文件的上传状态
    * 从未上传
    * 已经上传完成
    * 上传一部分，断点续传
    * */
    public static CommonResponseDTO checkFileMd5(String md5){
        CommonResponseDTO commonResponseDTO = new CommonResponseDTO();
        Object processingObj = stringRedisTemplate.opsForHash()
                .get(Constants.FILE_UPLOAD_STATUS, md5);    //上传文件的状态

        if (processingObj == null) {    //该文件从未上传
            commonResponseDTO.setResultVo(new ResultVo(ResultStatus.NO_HAVE));
            commonResponseDTO.setMessage("文件未上传,马上上传");

            return commonResponseDTO;
        }

        String processingStr = processingObj.toString();
        boolean processing = Boolean.parseBoolean(processingStr);
        String value = stringRedisTemplate.opsForValue()
                .get(Constants.FILE_MD5_KEY + md5); //文件所在路径

        if (processing) {   //上传完成
            commonResponseDTO.setResultVo(new ResultVo(ResultStatus.IS_HAVE));
            commonResponseDTO.setMessage("文件已经上传");

            return commonResponseDTO;
        } else {    //上传未完成
            File confFile = new File(value);
            byte[] completeList = new byte[0];
            try {
                completeList = FileUtils.readFileToByteArray(confFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<String> missChunkList = new LinkedList<>();    //文件未上传的部分

            for (int i = 0; i < completeList.length; i++) {
                if (completeList[i] != Byte.MAX_VALUE) {
                    missChunkList.add(i + "");
                }
            }

            commonResponseDTO.setResultVo(new ResultVo(ResultStatus.ING_HAVE));
            commonResponseDTO.setMessage("文件上传一部分，开始断点续传");

            return commonResponseDTO;
    }
    }

    /**
     * 在MappedByteBuffer释放后再对它进行读操作的话就会引发jvm crash，在并发情况下很容易发生
     * 正在释放时另一个线程正开始读取，于是crash就发生了。所以为了系统稳定性释放前一般需要检查是否还有线程在读或写
     *
     * @param mappedByteBuffer
     */
    public static void freedMappedByteBuffer(final MappedByteBuffer mappedByteBuffer) {
        try {
            if (mappedByteBuffer == null) {
                return;
            }

            mappedByteBuffer.force();
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    try {
                        Method getCleanerMethod = mappedByteBuffer.getClass().getMethod("cleaner", new Class[0]);
                        getCleanerMethod.setAccessible(true);
                        sun.misc.Cleaner cleaner = (sun.misc.Cleaner) getCleanerMethod.invoke(mappedByteBuffer,
                                new Object[0]);
                        cleaner.clean();
                    } catch (Exception e) {
                        logger.error("clean MappedByteBuffer error!!!", e);
                    }
                    logger.info("clean MappedByteBuffer completed!!!");
                    return null;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostConstruct
    public void init(){
        stringRedisTemplate = this.stringRedisTemplate1;
    }
}
