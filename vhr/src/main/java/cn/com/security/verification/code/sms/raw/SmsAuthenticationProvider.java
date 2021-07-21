package cn.com.security.verification.code.sms.raw;

import cn.com.security.service.ImgCodeUserDetailService;
import cn.com.security.service.SmsCodeUserDetailService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

/**
 * @author wyl
 * @create 2020-08-04 14:02
 */
public class SmsAuthenticationProvider implements AuthenticationProvider {

    private SmsCodeUserDetailService userDetailService;

    public SmsAuthenticationProvider(SmsCodeUserDetailService userDetailService) {
        this.userDetailService = userDetailService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        UserDetails loadedUser = this.userDetailService.loadUserByUsername((String)((SmsAuthenticationToken)authentication).getPrincipal());
        if (loadedUser == null || CollectionUtils.isEmpty(loadedUser.getAuthorities())) {
            throw new InternalAuthenticationServiceException(
                    "UserDetailsService returned null, which is an interface contract violation");
        }

        SmsAuthenticationToken result = new SmsAuthenticationToken(loadedUser,loadedUser.getAuthorities());
        result.setDetails(authentication.getDetails());

        return result;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SmsAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
