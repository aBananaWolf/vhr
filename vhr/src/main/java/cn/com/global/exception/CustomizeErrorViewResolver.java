package cn.com.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.DefaultErrorViewResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author wyl
 * @create 2020-08-07 11:56
 */
@Slf4j
public class CustomizeErrorViewResolver extends DefaultErrorViewResolver {

    private static final Map<HttpStatus.Series, String> SERIES_VIEWS;

    static {
        Map<HttpStatus.Series, String> views = new EnumMap<>(HttpStatus.Series.class);
        views.put(HttpStatus.Series.CLIENT_ERROR, "4xx");
        views.put(HttpStatus.Series.SERVER_ERROR, "5xx");
        SERIES_VIEWS = Collections.unmodifiableMap(views);
    }

    private final ApplicationContext applicationContext;
    private final ResourceProperties resourceProperties;

    /**
     * Create a new {@link DefaultErrorViewResolver} instance.
     * @param applicationContext the source application context
     * @param resourceProperties resource properties
     */
    public CustomizeErrorViewResolver(ApplicationContext applicationContext, ResourceProperties resourceProperties) {
        super(applicationContext, resourceProperties);
        this.applicationContext = applicationContext;
        this.resourceProperties = resourceProperties;
    }

    @Override
    public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
        // 首先自我处理，不行就委派上级，我们仅处理客户端异常
        if (SERIES_VIEWS.containsKey(status.series())) {
            String errorStatus = SERIES_VIEWS.get(status.series());
            if (errorStatus.equals("4xx")) {
                if (log.isInfoEnabled()) {
                    log.info("用户非法跳转：数据模型(model) " + model + request.getRequestURL());
                }
                String scheme = request.getScheme();
                String serverName = request.getServerName();
                int serverPort = request.getServerPort();
                String contextPath = request.getContextPath();

//                return new ModelAndView("redirect:http://localhost:8081/index.html#/hrinfo");
                return new ModelAndView("redirect:" +scheme + "://" + serverName + ":" + serverPort + contextPath + "/" + "index.html#/hrinfo");
            }
            // 500 就是大问题了，但我不处理
        }
        // 父类唯一公开的方法
        return super.resolveErrorView(request,status,model);
    }
}
