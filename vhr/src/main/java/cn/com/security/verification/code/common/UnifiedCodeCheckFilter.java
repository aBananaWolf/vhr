package cn.com.security.verification.code.common;

import cn.com.util.MyUrlUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * 收集所有的CodeCheckService并进行验证码验证逻辑
 * 它做了以下几件事
 * 1.将json请求参数转换为具体参数
 * 2.统一添加验证码校验流程
 * 3.校验失败统一处理
 * @author wyl
 * @create 2020-08-07 10:01
 */
@Slf4j
@Component
@Import(UnifiedCodeCheckFilter.CollectAbstractCodeCheckFilter.class)
public class UnifiedCodeCheckFilter extends OncePerRequestFilter {
    @Autowired
    private ObjectMapper objectMapper;
    public static final String IS_CHECK_FILTER_CHAIN = "IS_CHECK_FILTER_CHAIN";
    private List<CodeCheckService> codeCheckFilters;

    public void setCodeCheckFilters(List<CodeCheckService> codeCheckFilters) {
        this.codeCheckFilters = codeCheckFilters;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // post请求
        if (StringUtils.equalsIgnoreCase(request.getMethod(), "POST")) {
            request.setAttribute(IS_CHECK_FILTER_CHAIN,IS_CHECK_FILTER_CHAIN);
            try {
                Iterator<CodeCheckService> iterator = codeCheckFilters.iterator();
                while (iterator.hasNext()) {
                    CodeCheckService next = iterator.next();
                    // 匹配url
                    if (MyUrlUtils.urlEquals(request, next.doFilterProcessorUri())) {
                        if (log.isInfoEnabled()) {
                            log.info("进入了验证码校验流程");
                        }
                        // 包装并转换json为直接参数，供AbstractAuthenticationProcessorFilter 处理
                        // 这一步只有认证逻辑需要，需要严格通过以上判断
                        request = new Json2ParameterHttpServletRequestWrapper(request, objectMapper);
                        try {
                            this.checkCode(request, response, next);
                        } catch (AuthenticationException e) {
                            // 跳转失败认证逻辑
                            next.getLoginFailureHandler().onAuthenticationFailure(request, response, e);
                            // 不再进入过滤链
                            return;
                        } catch (Exception e) {
                            // 错误日志
                            if (log.isErrorEnabled()) {
                                next.catchErrorLog(e);
                            }
                            throw e;
                        }
                    }
                }
            } finally {
                request.removeAttribute(IS_CHECK_FILTER_CHAIN);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void checkCode(HttpServletRequest request, HttpServletResponse response, CodeCheckService next) throws IOException {
        CodeOperationService codeOperationService = next.getCodeOperationService();
        String code = codeOperationService.obtainCode(request);
        CodeDetails cacheCode = codeOperationService.getCode(request);
        try {
            // 前置处理，短信验证码中锁定了用户，就算是发送空值，也要累计发送次数进行锁定
            codeOperationService.applyPreProcess(request, cacheCode);

            // 下面这两个步骤都是要检测的，非空才能继续
            if (code == null) {
                throw next.throwCheckedException("验证码不能为空");
            }
            if (cacheCode == null) {
                throw next.throwCheckedException("请重新获取验证码");
            }

            if (cacheCode.isExpire()) {
                codeOperationService.removeCode(request);
                throw next.throwCheckedException("验证码过期");
            }
            if (!cacheCode.equals(code)) {
                if (cacheCode.maximumCheckCount() == null) {
                    throw next.throwCheckedException("验证码输入错误");
                } else if (cacheCode.maximumCheckCount() >= 0) {
                    throw next.throwCheckedException("验证码输入错误，还剩下 " + (cacheCode.maximumCheckCount() + 1) + " 次机会");
                } else {
                    throw next.throwCheckedException("验证码输入错误，" + cacheCode.userLockedTips());
                }
            }
            codeOperationService.removeCode(request);

            // 后置处理
            codeOperationService.applyPostProcess(request, cacheCode);
            if (log.isInfoEnabled()) {
                log.info("验证码校验成功");
            }
        } finally {
            // 最终处理
            codeOperationService.applyAfterProcess(request, cacheCode);
        }
    }

    static class CollectAbstractCodeCheckFilter implements ImportBeanDefinitionRegistrar, BeanFactoryAware {
         private ListableBeanFactory beanFactory;

         @Override
         public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
              if (beanFactory instanceof ListableBeanFactory) {
                  this.beanFactory = (ListableBeanFactory)beanFactory;
              }
         }

         @Override
         public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
             if (beanFactory == null) {
                 return;
             }
             registerSyntheticBeanIfMissing(registry,"unifiedCodeCheckFilterBeanPostProcessor", UnifiedCodeCheckFilterBeanPostProcessor.class);

         }
         private void registerSyntheticBeanIfMissing(BeanDefinitionRegistry registry, String name, Class<?> beanClass) {
             // 第二个参数忽略FactoryBean的内部getObject
             if (ObjectUtils.isEmpty(this.beanFactory.getBeanNamesForType(beanClass, true, false))) {
                 RootBeanDefinition beanDefinition = new RootBeanDefinition(beanClass);
                 beanDefinition.setSynthetic(true);
                 registry.registerBeanDefinition(name, beanDefinition);
             }
         }
     }
}
