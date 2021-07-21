package cn.com.security.verification.code.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wyl
 * @create 2020-08-03 11:13
 */
@Slf4j
public class Json2ParameterHttpServletRequestWrapper extends HttpServletRequestWrapper {
    private Map<String,String> map;
    private static final Type type;

    static {
        Type temp = null;
        try {
            temp = Json2ParameterHttpServletRequestWrapper.class.getDeclaredField("map").getGenericType();
        } catch (NoSuchFieldException e) {
            if (log.isErrorEnabled())
                log.error("Json2ParameterHttpServletRequestWrapper初始化异常",e);
        }
        type = temp;
    }

    public Json2ParameterHttpServletRequestWrapper(HttpServletRequest request, ObjectMapper objectMapper) {
        super(request);
        try {
            // 这里需要转换成String，不需要其它类型
            map = objectMapper.readValue(request.getInputStream(), new TypeReference<Map<String, String>>() {
                @Override
                public Type getType() {
                    return type;
                }
            });
        } catch (IOException e) {
            if (log.isErrorEnabled())
                log.error("json转换异常",e);
        }
    }

    @Override
    public String getParameter(String name) {
        String s = map.get(name);
        if (StringUtils.isEmpty(s))
            s = super.getParameter(name);
        return s;
    }
}
