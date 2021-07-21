package cn.com.security.session;

import cn.com.vo.RespBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author wyl
 * @create 2020-08-02 19:06
 */
@Component
public class CustomizeSessionExpireStrategy implements SessionInformationExpiredStrategy {
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException, ServletException {
        event.getResponse().setCharacterEncoding("UTF-8");
        event.getResponse().setContentType("application/json");
        event.getResponse().setStatus(401);
        RespBean respBean = RespBean.error("会话失效，你已被强制登出");

        PrintWriter writer = event.getResponse().getWriter();
        writer.write(objectMapper.writeValueAsString(respBean));
        writer.flush();
        writer.close();
    }
}
