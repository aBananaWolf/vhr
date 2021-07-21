package cn.com.security.verification.code.common;

import org.apache.commons.lang3.StringUtils;

/**
 * 验证码实体接口
 */
public interface CodeDetails {
    void eraseCredentials();

    boolean isExpire();

    boolean equals(String code);

    /**
     * 可以为空，表示不使用最大次数限制
     */
    Integer maximumCheckCount();

    /**
     * 表示不使用最大次数限制,区分保存和检查
     */
    Integer maximumSaveCount();

    /**
     * 如果 maximumCount 为空则不需要实现该方法，这个方法提供了最大次数限制达到之后的提示
     */
    String userLockedTips();
}
