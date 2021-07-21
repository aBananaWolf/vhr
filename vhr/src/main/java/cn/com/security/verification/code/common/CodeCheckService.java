package cn.com.security.verification.code.common;

import cn.com.security.config.WebSecurityConfig;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * 统一验证码服务
 * @author wyl
 * @create 2020-08-05 11:00
 */
public interface CodeCheckService {

    /**
     * 错误日志，区分是哪种验证码错误
     * @param e
     */
    void catchErrorLog(Exception e);

    CodeOperationService getCodeOperationService();

    AuthenticationFailureHandler getLoginFailureHandler();

    /**
     * UnifiedCodeCheckFilter将抛出的异常
     * @param errorMessage
     * @return
     */
    AuthenticationException throwCheckedException(String errorMessage);

    /**
     * 这个路径要特别注意一下，要在
     * {@link WebSecurityConfig#configure(WebSecurity)}
     * 中特别配置一下忽略路径，这个是不需要进入过滤链的
     *
     * @return
     */
    String doFilterProcessorUri();

}
