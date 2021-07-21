package cn.com.security.verification.code.img.session;

import cn.com.security.verification.code.img.common.AbstractVerifyCodeService;
import cn.com.security.verification.code.img.exception.ImageCodeCheckException;
import cn.com.security.verification.code.img.raw.ImageCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.concurrent.TimeUnit;

/**
 * @author wyl
 * @create 2020-08-02 18:38
 */
@Slf4j
@Component
public class SessionVerifyCodeService extends AbstractVerifyCodeService {

    public static final String SESSION_KEY_IMAGE_CODE = "SESSION_KEY_IMAGE_CODE";

    @Override
    protected void doSaveCode(ImageCode imageCode, long expireTime, TimeUnit timeUnit, String credential, HttpServletRequest request) throws JsonProcessingException {
        HttpSession session = request.getSession();
        request.getSession();
        session.setAttribute(credential,imageCode);
    }


    @Override
    protected ImageCode doGetImageCode(String credential, HttpServletRequest request)  throws JsonProcessingException, AuthenticationException{
        HttpSession session = request.getSession(false);
        ImageCode imageCode;
        if (session == null || (imageCode = (ImageCode)session.getAttribute(credential)) == null) {
            throw new ImageCodeCheckException("验证码过期，请重试");
        }
        return imageCode;
    }

    @Override
    protected void doRemoveCode(HttpServletRequest request, String credential) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(credential);
        }
    }

    @Override
    protected String getCredential(HttpServletRequest request) throws AuthenticationException {
// 前端并没有传需要的credential参数
//        try {
//            super.getCredential(request);
//        } catch (AuthenticationException e) {
//            if (log.isWarnEnabled()) {
//                log.warn("没有请求参数");
//            }
//        }
        return SESSION_KEY_IMAGE_CODE;
    }
}
