package cn.iecas.datasets.image;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author vanishrain
 */

@EnableTransactionManagement
@SpringBootApplication
public class MainApplication {
    public static void main(String[] args){
        SpringApplication.run(MainApplication.class);
    }
}
