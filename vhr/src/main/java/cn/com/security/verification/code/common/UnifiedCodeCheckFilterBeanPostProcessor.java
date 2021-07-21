package cn.com.security.verification.code.common;

import com.aliyuncs.utils.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wyl
 * @create 2020-08-07 10:10
 */
public class UnifiedCodeCheckFilterBeanPostProcessor implements BeanPostProcessor {
    private ListableBeanFactory beanFactory;

    public UnifiedCodeCheckFilterBeanPostProcessor(ListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * 收集所有的CodeCheckService 设置到 UnifiedCodeCheckFilter
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof UnifiedCodeCheckFilter) {
            Map<String, CodeCheckService> map = beanFactory.getBeansOfType(CodeCheckService.class, true, false);
            if ( map.size() <= 0) {
                return bean;
            }
            ArrayList<CodeCheckService> delegateCodeCheckService = new ArrayList<>(map.size());
            for (Map.Entry<String, CodeCheckService> entry : map.entrySet()) {
                delegateCodeCheckService.add(entry.getValue());
            }

            UnifiedCodeCheckFilter unifiedCodeCheckFilter = (UnifiedCodeCheckFilter) bean;
            unifiedCodeCheckFilter.setCodeCheckFilters(delegateCodeCheckService);
            return unifiedCodeCheckFilter;
        }
        return bean;
    }
}
