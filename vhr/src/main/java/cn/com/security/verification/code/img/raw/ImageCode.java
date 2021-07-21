package cn.com.security.verification.code.img.raw;

import cn.com.security.verification.code.common.CodeDetails;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

/**
 * @author wyl
 * @create 2020-08-02 16:09
 */
@Setter
@Getter
public class ImageCode implements Serializable, CodeDetails {

    private static final long serialVersionUID = -5802969747908342490L;

    private long expireTime;
    private String code;
    private long initialTime;
    private TimeUnit timeUnit;
    transient private BufferedImage bufferedImage;

    public ImageCode() {
    }

    public ImageCode(long expireTime, TimeUnit timeUnit, String code, BufferedImage bufferedImage) {
        this.initialTime = expireTime;
        this.timeUnit = timeUnit;
        this.expireTime = System.currentTimeMillis() + timeUnit.toMillis(expireTime);
        this.code = code;
        this.bufferedImage = bufferedImage;
    }

    public ImageCode(long expireTime, String code, BufferedImage bufferedImage) {
       this(expireTime,TimeUnit.SECONDS,code,bufferedImage);
    }

    public void eraseCredentials() {
        this.timeUnit = null;
        this.bufferedImage = null;
    }

    public boolean isExpire() {
        return this.getExpireTime() < System.currentTimeMillis();
    }

    public boolean equals(String code) {
        return StringUtils.equalsIgnoreCase(code,this.getCode());
    }

    @Override
    public Integer maximumCheckCount() {
        return null;
    }

    @Override
    public Integer maximumSaveCount() {
        return null;
    }

    @Override
    public String userLockedTips() {
        return null;
    }
}
