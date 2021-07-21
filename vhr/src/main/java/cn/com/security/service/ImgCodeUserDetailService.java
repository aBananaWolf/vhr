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
 * @create 2020-08-02 19:50
 */
@Component
@Slf4j
public class ImgCodeUserDetailService implements UserDetailsService, UserNotFoundExceptionTip {
    @Autowired
    private HrService hrService;
    @Autowired
    private CheckUserDetailService checkUserDetailService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        HrBO hrEntity = new HrBO();
        hrEntity.setUsername(username);
        List<HrBO> hrs = hrService.listHrAndRoleByHrBO(hrEntity);
        return checkUserDetailService.checkAndReturn(this,hrs);
    }

    @Override
    public String notFoundTip() {
        return "用户名或密码错误";
    }
}
