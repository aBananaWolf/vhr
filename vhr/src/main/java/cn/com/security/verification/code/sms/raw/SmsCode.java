package cn.com.security.verification.code.sms.raw;

import cn.com.security.verification.code.common.CodeDetails;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * @author wyl
 * @create 2020-08-04 14:08
 */
@Setter
@Getter
public class SmsCode implements Serializable, CodeDetails {

    private static final long serialVersionUID = -5802969747908342490L;

    private long expireTime;
    private String code;
    private long initialTime;
    private TimeUnit timeUnit;
    private Integer maximumCheckCount;
    private Integer maximumSaveCount;

    public SmsCode() {
    }

    public SmsCode(long expireTime, TimeUnit timeUnit, String code) {
        this.initialTime = expireTime;
        this.timeUnit = timeUnit;
        this.expireTime = System.currentTimeMillis() + timeUnit.toMillis(expireTime);
        this.code = code;
    }

    public SmsCode(long expireTime, String code) {
        this(expireTime,TimeUnit.SECONDS,code);
    }

    public void eraseCredentials() {
        this.timeUnit = null;
    }

    public boolean isExpire() {
        return this.getExpireTime() < System.currentTimeMillis();
    }

    public boolean equals(String code) {
        return StringUtils.equalsIgnoreCase(code,this.getCode());
    }

    @Override
    public Integer maximumCheckCount() {
        return maximumCheckCount;
    }

    @Override
    public Integer maximumSaveCount() {
        return maximumSaveCount;
    }

    @Override
    public String userLockedTips() {
        return "24小时内无法再次发送验证码";
    }
}
