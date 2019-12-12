package cn.iecas.datasets.image;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.*;

/**
 * @author vanishrain
 */

@EnableTransactionManagement
@SpringBootApplication
public class MainApplication {
    public static void main(String[] args) throws IOException, InterruptedException {
        SpringApplication.run(MainApplication.class);
//        String str = "picture\\8.jpg";
//        String fileExtName = str.substring(str.lastIndexOf("\\"));
//        System.out.println(StringUtils.substringAfter(str,"\\"));
//        System.out.println(fileExtName);
    }
}
