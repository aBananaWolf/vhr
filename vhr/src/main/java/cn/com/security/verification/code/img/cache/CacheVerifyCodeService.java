package cn.com.security.verification.code.img.cache;

import cn.com.security.verification.code.img.common.AbstractVerifyCodeService;
import cn.com.security.verification.code.img.exception.ImageCodeCheckException;
import cn.com.security.verification.code.img.raw.ImageCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * 这个项目的前端没有传入redis要的username参数，暂时无法启用
 * @author wyl
 * @create 2020-08-02 17:29
 */
//@Component
public class CacheVerifyCodeService extends AbstractVerifyCodeService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    public static final String IMAGE_CODE_PREFIX = "imgVerificationCode:";

    @Override
    protected void doSaveCode(ImageCode imageCode, long expireTime, TimeUnit timeUnit, String credential, HttpServletRequest request) throws JsonProcessingException {
        redisTemplate.opsForValue().set(credential,objectMapper.writeValueAsString(imageCode),expireTime,timeUnit);
    }


    @Override
    protected ImageCode doGetImageCode(String credential, HttpServletRequest request)  throws JsonProcessingException, AuthenticationException{
        String codeStr = redisTemplate.opsForValue().get(credential);
        if (StringUtils.isBlank(codeStr)) {
            throw new ImageCodeCheckException("验证码过期");
        }
        return objectMapper.readValue(codeStr,ImageCode.class);
    }

    @Override
    protected String getCredential(HttpServletRequest request) throws AuthenticationException {
// 前端并没有传需要的credential参数
//        String param = request.getParameter(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY);
//        if (param == null) {
//            throw new AuthenticationServiceException("没有请求参数");
//        }
        return IMAGE_CODE_PREFIX;
    }
}
