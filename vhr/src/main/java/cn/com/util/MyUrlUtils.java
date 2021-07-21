package cn.com.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

/**
 * 虚拟路径问题 contextPath
 * @author wyl
 * @create 2020-08-02 14:44
 */
public class MyUrlUtils {
    public static String getActualUrl(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestURI = request.getRequestURI();
        if (StringUtils.equals(contextPath,"/"))
            return requestURI;
        return requestURI.substring(contextPath.length());
    }

    public static boolean urlEquals(HttpServletRequest request,@NotNull String withOutContextPathStr) {
        if (request == null) {
            return false;
        }
        return getActualUrl(request).equals(withOutContextPathStr);
    }
}
