package cn.com.service;

import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.security.core.session.SessionInformation;

import java.util.*;

/**
 * 适配分层模块用的，没什么意义
 */
public interface CacheSessionRegistryService {

    public List<Object> getAllPrincipals();

    public List<SessionInformation> getAllSessions(Object principal, boolean includeExpiredSessions);

    public SessionInformation getSessionInformation(String sessionId);

    public void refreshLastRequest(String sessionId);

    public void registerNewSession(String sessionId, Object principal);

    public void removeSessionInformation(String sessionId);

    public void onApplicationEvent(SessionDestroyedEvent event);

    public void multiUpdateSessionInformation(List<SessionInformation> sessionInformationList);

    public void kickOutUser(Object principal);
}

