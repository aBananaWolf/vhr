package cn.com.security.verification.code.sms.cache;

import cn.com.entities.SmsVerificationCodeSendLogEntity;
import cn.com.mq.sender.SmsCodeSender;
import cn.com.security.verification.code.sms.common.AbstractSmsCodeService;
import cn.com.security.verification.code.sms.raw.SmsCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * @author wyl
 * @create 2020-08-04 15:13
 */
@Slf4j
@Component
public class CacheSmsCodeService extends AbstractSmsCodeService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SmsCodeSender smsCodeSender;

    public final static String SMS_CODE_PREFIX = "smsVerificationCode:";
    // 检查次数
    public final static String MAXIMUM_COUNT_OF_CHECK = "smsVerificationCode:maximumNumberOfCheck:";
    // 发送短信的计数
    public final static String MAXIMUM_COUNT_OF_SEND = "smsVerificationCode:maximumNumberOfSend:";
    // 锁
    public final static String SMS_MAXIMUM_LOCK = "smsVerificationCode:lock:";


    @Override
    protected void doSendCode(String code, String phoneStr) throws Exception {
        SmsVerificationCodeSendLogEntity smsVerificationCodeSendLogEntity = new SmsVerificationCodeSendLogEntity();
        smsVerificationCodeSendLogEntity.setCode(code);
        smsVerificationCodeSendLogEntity.setPhone(Long.parseLong(phoneStr));
        smsCodeSender.sendSmsVerificationCodeAndSaveLog(smsVerificationCodeSendLogEntity);
    }

    @Override
    protected void doSaveCode(SmsCode smsCode, String credential, long expireTime, TimeUnit timeUnit, HttpServletRequest request) throws JsonProcessingException {
        redisTemplate.opsForValue().set(credential,objectMapper.writeValueAsString(smsCode),expireTime,timeUnit);
    }

    @Override
    protected SmsCode doGetSmsCode(String credential, HttpServletRequest request) throws JsonProcessingException, AuthenticationException {
        String param = redisTemplate.opsForValue().get(credential);
        if (StringUtils.isEmpty(param)) {
            throw new AuthenticationServiceException("没有缓存数据");
        }
        return objectMapper.readValue(param,SmsCode.class);
    }

    @Override
    public String getCredential(HttpServletRequest request) throws AuthenticationException {
        return SMS_CODE_PREFIX + super.getCredential(request);
    }

    @Override
    protected String getSmsMaximumLock() {
        return SMS_MAXIMUM_LOCK;
    }

    @Override
    protected StringRedisTemplate getStringRedisTemplate() {
        return redisTemplate;
    }

    @Override
    protected String doGetMaximumSendCredential(HttpServletRequest request) {
        return MAXIMUM_COUNT_OF_SEND + super.getCredential(request);
    }

    @Override
    protected String doGetMaximumCheckCredential(HttpServletRequest request) {
        return MAXIMUM_COUNT_OF_CHECK + super.getCredential(request);
    }
}
