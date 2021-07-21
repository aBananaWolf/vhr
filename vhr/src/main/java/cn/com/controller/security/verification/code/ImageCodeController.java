package cn.com.controller.security.verification.code;

import cn.com.constant.exception.WarnEnum;
import cn.com.exception.UserIllegalOperationException;
import cn.com.security.handler.LoginFailureHandler;
import cn.com.security.verification.code.img.common.AbstractVerifyCodeService;
import cn.com.security.verification.code.img.raw.ImageCode;
import cn.com.security.verification.code.img.util.ImageCodeGeneratorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author wyl
 * @create 2020-08-02 16:52
 */
@Slf4j
@RestController
public class ImageCodeController {
    @Autowired
    private ImageCodeGeneratorUtil imageCodeGeneratorUtil;
    @Autowired
    private AbstractVerifyCodeService verifyCodeService;
    @Autowired
    // 由于servlet 的机制，不太适合用authenticationFailureHandler
    private LoginFailureHandler loginFailureHandler;

    @GetMapping("/verifyCode")
    public void generatorCode(HttpServletRequest request, HttpServletResponse response) {
        ImageCode code = imageCodeGeneratorUtil.createCode(300);
        try {
            BufferedImage img = code.getBufferedImage();
            // 前端不会传入错误参数，不需要特殊处理请求
            verifyCodeService.saveCode(code, request);
            // 最后再获取outputStream(),在获取outputStream的时候会关闭资源，不再允许操作session
            ImageIO.write(img, "jpeg", response.getOutputStream());
        } catch (IOException e) {
            if (log.isErrorEnabled())
                log.error("可能是用户非法操作，生成验证码产生IO异常",e);
        } catch (AuthenticationException e) {
            if (log.isErrorEnabled())
                log.error("可能是用户非法操作，生成验证码产生异常",e);
        } catch (Exception e) {
            // 统一处理
            throw new UserIllegalOperationException(WarnEnum.ILLEGAL_OPERATION);
        }
    }
}
