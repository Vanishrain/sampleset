package cn.iecas.datasets.image.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 文件md5值
 */
@Component
public class FileMD5Util {
    private final static Logger logger = LoggerFactory.getLogger(FileMD5Util.class);
    @Autowired
    private StringRedisTemplate stringRedisTemplate1;
    private static StringRedisTemplate stringRedisTemplate;





    @PostConstruct
    public void init(){
        stringRedisTemplate = this.stringRedisTemplate1;
    }
}
