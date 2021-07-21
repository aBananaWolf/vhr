package cn.com.security.service;

import cn.com.bo.Hr;
import cn.com.bo.HrBO;
import cn.com.mapstruct.HrEntity2BO;
import cn.com.bo.CustomizeGrantedAuthority;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author wyl
 * @create 2020-08-10 11:00
 */
@Slf4j
@Component
public class CheckUserDetailService {
    public UserDetails checkAndReturn(UserNotFoundExceptionTip userNotFoundExceptionTip, List<HrBO> hrs) {
        if (CollectionUtils.isEmpty(hrs)) {
            throw new UsernameNotFoundException(userNotFoundExceptionTip.notFoundTip());
        }
        if (hrs.size() > 1) {
            log.error("系统内部数据错误，查询出了 " + hrs.size() + " 条数据");
            throw new UsernameNotFoundException("没有找到指定用户");
        }

        HrBO hr = hrs.get(0);
        if (!hr.getEnabled()) {
            throw new DisabledException("账号被禁用，请联系管理员");
        }
        Hr contextHr = HrEntity2BO.INSTANCE.hrEntity2BO(hr);
        List<CustomizeGrantedAuthority> authorities = hr.getRoles().stream().map(role -> new CustomizeGrantedAuthority(role.getName())).collect(toList());
        contextHr.setAuthorities(authorities);
        return contextHr;
    }

}
