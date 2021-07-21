package cn.com;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author wyl
 * @create 2020-08-20 10:28
 */
@SpringBootApplication
@MapperScan("cn.com.dao")
public class TimedTaskMain {

    public static void main(String[] args) {
        SpringApplication.run(TimedTaskMain.class, args);
    }
}
