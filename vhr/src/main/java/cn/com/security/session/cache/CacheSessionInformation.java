package cn.com.security.session.cache;

import lombok.Data;
import org.jacoco.agent.rt.internal_43f5073.core.data.SessionInfo;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.util.Assert;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.Serializable;
import java.util.Date;

/**
 * 集成SessionInformation，这个类需要被反序列化，因此这里是解决无参数构造器问题
 * @author wyl
 * @create 2020-08-11 21:30
 */
public class CacheSessionInformation extends SessionInformation {
    private static final String empty = "field";
    private static final Date date = new Date();

    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    // ~ Instance fields
    // ================================================================================================

    private Date lastRequest;
    private Object principal;
    private String sessionId;
    private boolean expired = false;

    public CacheSessionInformation() {
        super(empty, empty, date);
    }

    public CacheSessionInformation(Object principal, String sessionId, Date lastRequest) {
        super(empty, empty, date);
        this.principal = principal;
        this.sessionId = sessionId;
        this.lastRequest = lastRequest;
    }

    @Override
    public void expireNow() {
        this.expired = true;
    }

    @Override
    public Date getLastRequest() {
        return lastRequest;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public boolean isExpired() {
        return expired;
    }

    @Override
    public void refreshLastRequest() {
        this.lastRequest = new Date();
    }

    public void setLastRequest(Date lastRequest) {
        this.lastRequest = lastRequest;
    }

    public void setPrincipal(Object principal) {
        this.principal = principal;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }
}
