package cn.com.global.exception;

import jdk.nashorn.internal.objects.annotations.Getter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author wyl
 * @create 2020-08-05 09:52
 */
@RestController
public class ExceptionTest {

    @GetMapping("/my/error")
    public void test() {
        throw new RuntimeException("异常测试？");
    }
}
