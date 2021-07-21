package cn.com.security.entrypoint;

import cn.com.security.handler.LoginFailureHandler;
import cn.com.util.MyUrlUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 因为前端采用了框架集来进行布局划分，Java项目中使用重定向无法跳转到登录页
 * 因此前端通过判断状态码和返回的json来进行手动跳转认证(前端首先就会请求/system/config/menu路径)
 * 我们要通过这个路径来判断是否真的需要重定向
 *
 * 经过处理过后，如果是错误的url会跳转到授权点，如果是直接进入前端框架集布局的恶意请求则返回RespBean的json交给前端解决
 * @author wyl
 * @create 2020-08-02 14:35
 */
@Slf4j
public class AuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint{
    private LoginFailureHandler loginFailureHandler;

    public AuthenticationEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    public void setLoginFailureHandler(LoginFailureHandler loginFailureHandler) {
        this.loginFailureHandler = loginFailureHandler;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        if (MyUrlUtils.urlEquals(request, "/system/config/menu")) {
            // 前端通过统一登录失败处理逻辑跳出框架集
            loginFailureHandler.onAuthenticationFailure(request, response, new AuthenticationServiceException("还没有登录，请联系管理员"));
            return;
        }
        super.commence(request, response, authException);
    }

    /* @Bean
    public LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint() {
        return new LoginUrlAuthenticationEntryPoint(SecurityConstant.LOGIN_ENTRY_POINT) {
            private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

            public void commence(HttpServletRequest request, HttpServletResponse response,
                                 AuthenticationException authException) throws IOException, ServletException {

                if (MyUrlUtils.urlEquals(request, "/system/config/menu")) {
                    // 前端通过统一登录失败处理逻辑跳出框架集
                    loginFailureHandler.onAuthenticationFailure(request, response, new AuthenticationServiceException("还没有登录，请联系管理员"));
                    return;
                }

                String redirectUrl = null;

                if (super.isUseForward()) {

                    if (super.isForceHttps() && "http".equals(request.getScheme())) {
                        // First redirect the current request to HTTPS.
                        // When that request is received, the forward to the login page will be
                        // used.
                        redirectUrl = buildHttpsRedirectUrlForRequest(request);
                    }

                    if (redirectUrl == null) {
                        String loginForm = determineUrlToUseForThisRequest(request, response,
                                authException);

                        if (log.isDebugEnabled()) {
                            log.debug("Server side forward to: " + loginForm);
                        }

                        RequestDispatcher dispatcher = request.getRequestDispatcher(loginForm);

                        dispatcher.forward(request, response);

                        return;
                    }
                } else {
                    // redirect to login page. Use https if forceHttps true

                    redirectUrl = buildRedirectUrlToLoginPage(request, response, authException);

                }

                redirectStrategy.sendRedirect(request, response, redirectUrl);
            }
        };
    }*/
}
