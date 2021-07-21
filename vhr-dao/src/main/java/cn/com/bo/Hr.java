package cn.com.bo;

import cn.com.entities.HrEntity;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 带权限的 userDetails，被存放入SecurityContextHolder.getContext().getAuthentication().principal中
 * @author wyl
 * @create 2020-08-05 15:38
 */
public class Hr extends HrEntity implements UserDetails, CredentialsContainer {
    private static final long serialVersionUID = 2243006728942760938L;

    private List<CustomizeGrantedAuthority> authorities;

    @Override
    public String getPassword() {
        return super.getPassword();
    }

    @Override
    public String getUsername() {
        return super.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return super.getEnabled();
    }

    public void setUsername(String username) {
        super.setUsername(username);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<CustomizeGrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public void eraseCredentials() {
        super.setPassword(null);
    }

    @Override
    public boolean equals(Object hr) {
        if (hr instanceof Hr) {
            return this.getUsername().equals(((Hr) hr).getUsername());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getUsername().hashCode();
    }
}
