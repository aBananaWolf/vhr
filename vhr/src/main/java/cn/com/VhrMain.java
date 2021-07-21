package cn.com;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author wyl
 * @create 2020-08-02 11:11
 */
@SpringBootApplication
@MapperScan("cn.com.dao")
public class VhrMain {

    public static void main(String[] args) {
        SpringApplication.run(VhrMain.class, args);
    }
}
