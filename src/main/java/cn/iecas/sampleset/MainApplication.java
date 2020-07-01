package cn.iecas.sampleset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author vanishrain
 */

@EnableAsync
@SpringBootApplication
@EnableTransactionManagement
public class MainApplication implements Cloneable {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class);
    }
}
