package cn.com.util;

import cn.com.bo.Hr;
import cn.com.entities.HrEntity;
import cn.com.mapstruct.HrEntity2BO;
import cn.com.security.verification.code.sms.raw.SmsAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 统一保存安全上下文
 * @author wyl
 * @create 2020-08-05 19:20
 */
public class AuthenticationJudgeUtil {
    /**
     * 保存上下文
     * @param authentication
     * @param hrEntity
     */
    public static void judgeAndSaveSecurityContext(Authentication authentication, HrEntity hrEntity) {
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            saveSecurityContext(new UsernamePasswordAuthenticationToken(toHr(hrEntity), authentication.getCredentials(), authentication.getAuthorities()));
        } else if (authentication instanceof SmsAuthenticationToken) {
            saveSecurityContext(new SmsAuthenticationToken(toHr(hrEntity), authentication.getAuthorities()));
        }
    }

    public static Integer getHrId() {
        return ((Hr)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }

    protected static void saveSecurityContext(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    protected static Hr toHr(HrEntity hrEntity) {
        return HrEntity2BO.INSTANCE.hrEntity2BO(hrEntity);
    }


}
