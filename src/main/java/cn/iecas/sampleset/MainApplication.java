package cn.iecas.sampleset;

import cn.iecas.sampleset.common.annotation.EnableSecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import sun.misc.Unsafe;

import java.util.HashMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * @author vanishrain
 */

@EnableAsync
//@EnableSecurity
@SpringBootApplication
@EnableTransactionManagement
public class MainApplication implements Cloneable {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class);
        System.out.println("sdf");
    }
}
