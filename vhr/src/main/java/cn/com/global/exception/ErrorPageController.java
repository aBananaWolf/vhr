package cn.com.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 4xx 5xx 全部跳转到HrInfo (不需要提示，暂时不用)
 * @author wyl
 * @create 2020-08-06 13:49
 */
@Slf4j
@Deprecated
public class ErrorPageController extends BasicErrorController {

    public ErrorPageController(ErrorAttributes errorAttributes, ErrorProperties errorProperties) {
        super(errorAttributes, errorProperties);
    }

    public ErrorPageController(ErrorAttributes errorAttributes, ErrorProperties errorProperties, List<ErrorViewResolver> errorViewResolvers) {
        super(errorAttributes, errorProperties, errorViewResolvers);
    }

    @Override
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        if (log.isInfoEnabled()) {
            HttpStatus status = super.getStatus(request);
            log.info("用户非法跳转：状态码 " + status + request.getRequestURL());
        }
        return new ModelAndView("redirect:http://localhost:8081/index.html#/hrinfo");
    }
}
