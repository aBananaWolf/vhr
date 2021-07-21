package cn.com.security.verification.code.img.raw;

import cn.com.security.constant.SecurityConstant;
import cn.com.security.handler.LoginFailureHandler;
import cn.com.security.verification.code.common.CodeCheckService;
import cn.com.security.verification.code.common.CodeOperationService;
import cn.com.security.verification.code.img.common.AbstractVerifyCodeService;
import cn.com.security.verification.code.img.exception.ImageCodeCheckException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @author wyl
 * @create 2020-08-02 14:00
 */
@Component
@Slf4j
public class ImageCodeCheckService implements CodeCheckService {
    @Autowired
    private AbstractVerifyCodeService verifyCodeService;
    @Autowired
    private LoginFailureHandler loginFailureHandler;

    @Override
    public void catchErrorLog(Exception e) {
        log.error("图片验证码校验异常！",e);
    }

    @Override
    public CodeOperationService getCodeOperationService() {
        return verifyCodeService;
    }

    @Override
    public AuthenticationFailureHandler getLoginFailureHandler() {
        return loginFailureHandler;
    }

    @Override
    public AuthenticationException throwCheckedException(String errorMessage) {
        return new ImageCodeCheckException(errorMessage);
    }

    @Override
    public String doFilterProcessorUri() {
        return SecurityConstant.IMAGE_CODE_LOGIN_PROCESSING_URL;
    }

}
