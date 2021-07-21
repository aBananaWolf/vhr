package cn.com.security.handler;

import cn.com.vo.RespBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author wyl
 * @create 2020-08-02 14:37
 */
@Component
public class CustomizeAccessDeniedHandler implements AccessDeniedHandler {
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        //        response.sendRedirect(request.getScheme() + request.getServerName() + request.getServerPort() + request.getContextPath() + SecurityProperties.LOGIN_ENTRY_POINT);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();

        writer.write(objectMapper.writeValueAsString(RespBean.ok("没有权限！")));

        writer.flush();
        writer.close();
    }
}
