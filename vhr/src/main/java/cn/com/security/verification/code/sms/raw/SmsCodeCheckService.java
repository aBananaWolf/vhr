package cn.com.security.verification.code.sms.raw;

import cn.com.security.constant.SecurityConstant;
import cn.com.security.handler.LoginFailureHandler;
import cn.com.security.verification.code.common.CodeCheckService;
import cn.com.security.verification.code.common.CodeOperationService;
import cn.com.security.verification.code.sms.common.AbstractSmsCodeService;
import cn.com.security.verification.code.sms.exception.SmsCodeCheckException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 校验验证码的服务
 * @author wyl
 * @create 2020-08-04 15:42
 */
@Slf4j
@Component
public class SmsCodeCheckService implements CodeCheckService {
    @Autowired
    private AbstractSmsCodeService smsCodeService;
    @Autowired
    private LoginFailureHandler loginFailureHandler;

    @Override
    public void catchErrorLog(Exception e) {
        log.error("短信验证码校验异常",e);
    }

    @Override
    public CodeOperationService getCodeOperationService() {
        return smsCodeService;
    }

    @Override
    public AuthenticationFailureHandler getLoginFailureHandler() {
        return loginFailureHandler;
    }

    @Override
    public AuthenticationException throwCheckedException(String errorMessage) {
        return new SmsCodeCheckException(errorMessage);
    }

    @Override
    public String doFilterProcessorUri() {
        return SecurityConstant.SMS_CODE_LOGIN_PROCESSING_URL;
    }

}
