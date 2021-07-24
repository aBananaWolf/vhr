package cn.com.security.verification.code.common;

import cn.com.security.verification.code.sms.common.AbstractSmsCodeService;
import cn.com.security.verification.code.sms.raw.SmsCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 验证码操作接口
 */
public interface CodeOperationService {
    String CODE_PARAM = "code";

    /**
     * 获取请求参数中的验证码
     * @param credential
     * @return
     */
    public String obtainCode(HttpServletRequest credential);

    /**
     * 能直接在 {@link WebSecurityConfigurerAdapter#configure(WebSecurity)}
     * 中进行忽略处理(web.ignoring().mvcMatchers("/verifyCode")
     *
     * 忽略处理后不再需要进入过滤链
     *
     * 控制发送次数和校验次数的短信验证码流程如下
     * {@link AbstractSmsCodeService#saveCode(SmsCode, HttpServletRequest)}
     *
     * @param code
     * @param credential
     */
    public void saveCode(CodeDetails code, HttpServletRequest credential) throws AuthenticationException;

    /**
     * 获取验证码
     * @return
     */
    public CodeDetails getCode(HttpServletRequest credential) throws AuthenticationException;

    /**
     * 选择实现删除功能
     */
    public void removeCode(HttpServletRequest credential);


    /**
     * 可以选择实现锁定用户功能
     * 示例：{@link AbstractSmsCodeService#applyPreProcess(HttpServletRequest, CodeDetails)(HttpServletRequest, CodeDetails, String)}
     *
     * @param request
     * @param cacheImageCode
     * @throws AuthenticationException
     * @throws IOException
     */
    public void applyPreProcess(HttpServletRequest request, CodeDetails cacheImageCode) throws AuthenticationException, IOException;
    public void applyPostProcess(HttpServletRequest request, CodeDetails cacheImageCode) throws AuthenticationException, IOException;
    public void applyAfterProcess(HttpServletRequest request, CodeDetails cacheImageCode) throws AuthenticationException, IOException;

    // 获取主要参数的方法，由抽象类统一管理，protected方法修饰符
    //    public String getCredential(HttpServletRequest request);
}
