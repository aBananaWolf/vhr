package cn.com.security.verification.code.img.common;

import cn.com.security.verification.code.common.CodeDetails;
import cn.com.security.verification.code.common.CodeOperationService;
import cn.com.security.verification.code.img.raw.ImageCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * 定义获取验证码和保存验证码的方法，忽略细节
 * @author wyl
 * @create 2020-08-02 17:39
 */
@Slf4j
public abstract class AbstractVerifyCodeService implements CodeOperationService {

    public String obtainCode(HttpServletRequest credential) {
        return credential.getParameter(CodeOperationService.CODE_PARAM);
    }

    @Override
    public void removeCode(HttpServletRequest credential) {
        String param = getCredential(credential);
        doRemoveCode(credential,param);
    }
    /**
     * 可选实现
     * @param credential
     */
    protected void doRemoveCode(HttpServletRequest request, String credential) {

    }
    @Override
    public void saveCode(CodeDetails code, HttpServletRequest credential) throws AuthenticationException {
        this.saveCode((ImageCode)code,credential);
    }

    protected void saveCode(ImageCode imageCode, HttpServletRequest credential) throws AuthenticationException {
        long expireTime = imageCode.getInitialTime();
        TimeUnit timeUnit = imageCode.getTimeUnit();
        imageCode.eraseCredentials();
        try {
            this.doSaveCode(imageCode,expireTime,timeUnit,this.getCredential(credential),credential);
        } catch (JsonProcessingException e) {
            if (log.isErrorEnabled()) {
                log.error("保存图片验证码出错了");
            }
            throw new AuthenticationServiceException("保存图片验证码出错了");
        }
    }
    // expireTime 和 TimeUnit 用于redis处理, session则不需要
    abstract protected void doSaveCode(ImageCode imageCode, long expireTime, TimeUnit timeUnit, String credential, HttpServletRequest httpServletRequest) throws JsonProcessingException;

    @Override
    public CodeDetails getCode(HttpServletRequest credential) throws AuthenticationException {
        String param = getCredential(credential);
        if (StringUtils.isEmpty(param)) {
            throw new AuthenticationServiceException("没有参数");
        }
        try {
            return this.doGetImageCode(param,credential);
        } catch (JsonProcessingException e) {
            if (log.isErrorEnabled())
                log.error("图片验证码json数据解析异常");
        }
        return null;
    }

    abstract protected ImageCode doGetImageCode(String credential, HttpServletRequest httpServletRequest) throws JsonProcessingException, AuthenticationException;

    /**
     * 重写这个方法将 do.. 方法的 credential 字符串替换成自定义的格式
     */
    protected String getCredential(HttpServletRequest request) throws AuthenticationException{
        String param = request.getParameter(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY);
        if (StringUtils.isEmpty(param)) {
            throw new AuthenticationServiceException("没有请求参数");
        }
        return param;
    }




}
