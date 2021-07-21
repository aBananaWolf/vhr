package cn.com.security.handler;

import cn.com.vo.RespBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author wyl
 * @create 2020-08-02 12:33
 */
@Slf4j
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {
    @Autowired
    private ObjectMapper objectMapper;

    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setStatus(401);
        RespBean resp = RespBean.error(e.getMessage());

        baseCheck(resp,e);

        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(resp));
        writer.flush();
        writer.close();
        //        response.sendRedirect("登录失败");
    }

    private void baseCheck(RespBean resp, AuthenticationException e) {
        if (e instanceof AccountExpiredException) {
            resp.setMsg("账号过期，请联系管理员");
        }
        else if (e instanceof DisabledException) {
            resp.setMsg("账号被禁用，请联系管理员");
        }
        else if (e instanceof LockedException) {
            resp.setMsg("账号被锁定，请联系管理员");
        }
        else if (e instanceof CredentialsExpiredException) {
            resp.setMsg("密码过期，请联系管理员");
        }
        else if (e instanceof BadCredentialsException) {
            resp.setMsg("用户名或密码错误");
        }
        if (log.isInfoEnabled()) {
            log.info(e.getMessage());
        }
    }
}
