package cn.com;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author wyl
 * @create 2020-08-13 16:56
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("cn.com.dao")
public class SmsMain {

    public static void main(String[] args) {
        SpringApplication.run(SmsMain.class, args);
    }
}
