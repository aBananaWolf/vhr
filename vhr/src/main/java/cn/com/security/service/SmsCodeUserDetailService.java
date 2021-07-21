package cn.com.security.service;

import cn.com.bo.HrBO;
import cn.com.service.HrService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author wyl
 * @create 2020-08-04 18:50
 */
@Slf4j
@Component
public class SmsCodeUserDetailService implements UserDetailsService, UserNotFoundExceptionTip {
    @Autowired
    private HrService hrService;
    @Autowired
    private CheckUserDetailService checkUserDetailService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        HrBO hrEntity = new HrBO();
        hrEntity.setPhone(username);
        List<HrBO> hrs = hrService.listHrAndRoleByHrBO(hrEntity);
        return checkUserDetailService.checkAndReturn(this,hrs);
    }

    @Override
    public String notFoundTip() {
        return "没有找到指定用户";
    }
}
