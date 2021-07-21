package cn.com.controller.security.verification.code;

import cn.com.security.verification.code.sms.common.AbstractSmsCodeService;
import cn.com.security.verification.code.sms.raw.SmsCode;
import cn.com.security.verification.code.sms.util.SmsCodeGeneratorUtil;
import cn.com.security.verification.code.sms.util.SmsCodeSenderUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 短信认证，修改成你的配置即可使用 {@link SmsCodeSenderUtils}
 *
 * GET 获取验证码 http://localhost:8081/sms/verifyCode?phone=13049394389
 *
 * POST 登录 localhost:8081/sms/doLogin  (json格式)
 * credential(json) :
 * {
 * 	"code": 159285, 必填
 * 	"phone": 13049394389, 必填
 * }
 *
 * @author wyl
 * @create 2020-08-04 14:26
 */
@Slf4j
@RestController
public class SmsCodeController {
    @Autowired
    private AbstractSmsCodeService smsCodeService;
    @Autowired
    private SmsCodeGeneratorUtil smsCodeGeneratorUtil;

    @GetMapping("/sms/verifyCode")
    public void GeneratorSmsCode(HttpServletRequest request, HttpServletResponse response) {
        SmsCode code = smsCodeGeneratorUtil.createCode(AbstractSmsCodeService.EXPIRE_TIME, AbstractSmsCodeService.EXPIRE_TIME_UNIT);
        try {
            smsCodeService.saveCode(code,request);
        } catch (AuthenticationException e) {
            if (log.isInfoEnabled()) {
                log.info("用户违规操作，生成验证码产生异常");
            }
            throw e;
        }
    }
}
