package cn.com.security.aware;

import org.apache.ibatis.javassist.ClassClassPath;
import org.apache.ibatis.javassist.ClassPool;
import org.apache.ibatis.javassist.CtClass;
import org.apache.ibatis.javassist.CtMethod;
import org.apache.ibatis.javassist.bytecode.CodeAttribute;
import org.apache.ibatis.javassist.bytecode.LocalVariableAttribute;
import org.apache.ibatis.javassist.bytecode.MethodInfo;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;
import java.util.Set;

/**
 * @author wyl
 * @create 2020-08-03 15:57
 */
@Component
public class ApplicationTest implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void printHandlerMapping() {

        RequestMappingHandlerMapping rmhp = applicationContext.getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> map = rmhp.getHandlerMethods();
        String result = "";
        for(RequestMappingInfo info : map.keySet()){
            result +=info.getPatternsCondition().toString().replace("[", "").replace("]", "")+ "\t"     ;
            HandlerMethod  hm=map.get(info);
            result +=hm.getBeanType().getName()+ "\t"   ;
            result +=hm.getBeanType().getName() + hm.getMethod().getName() + "\t";
            result +=info.getProducesCondition().toString().replace("[", "").replace("]", "")+ "\t"     ;
            result += "\r\n";
        }

    }

}
