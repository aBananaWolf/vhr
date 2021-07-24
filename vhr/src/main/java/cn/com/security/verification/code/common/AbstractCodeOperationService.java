package cn.com.security.verification.code.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author wyl
 * @create 2021-07-24 13:17
 */
public abstract class AbstractCodeOperationService implements CodeOperationService {
    @Override
    public void applyPreProcess(HttpServletRequest request, CodeDetails cacheImageCode) throws AuthenticationException, IOException{};
    @Override
    public void applyPostProcess(HttpServletRequest request, CodeDetails cacheImageCode) throws AuthenticationException, IOException{};
    @Override
    public void applyAfterProcess(HttpServletRequest request, CodeDetails cacheImageCode) throws AuthenticationException, IOException{};
}
