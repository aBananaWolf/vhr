package cn.com.security.constant;


/**
 * @author wyl
 * @create 2020-08-02 14:34
 */
public interface SecurityConstant {
    /**
     * 忽略路径
     */
    String[] IGNORE_URLS = {"/css/**", "/js/**",  "/index.html", "/img/**", "/fonts/**", "/favicon.ico", "/verifyCode", "/sms/verifyCode"};

    /**
     * 主页
     */
    String LOGIN_ENTRY_POINT = "/index.html";

    /**
     * 图片验证码的登录处理url
     */
    String IMAGE_CODE_LOGIN_PROCESSING_URL = "/doLogin";


    String SMS_CODE_LOGIN_PROCESSING_URL = "/sms/doLogin";
}
