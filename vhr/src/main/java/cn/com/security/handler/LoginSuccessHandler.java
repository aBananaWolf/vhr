package cn.com.security.handler;

import cn.com.vo.RespBean;
import cn.com.entities.HrEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author wyl
 * @create 2020-08-02 12:56
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    private ObjectMapper objectMapper;

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        this.ordinaryAuthenticationSuccessLogic(request,response,authentication);
    }

    public void ordinaryAuthenticationSuccessLogic(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();

        HrEntity principal = (HrEntity)authentication.getPrincipal();
        writer.write(objectMapper.writeValueAsString(RespBean.ok("登录成功",principal)));

        writer.flush();
        writer.close();
    }

    public void oauth2AuthenticationSuccessLogic(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

    }
}
