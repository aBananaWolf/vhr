package cn.com.global.exception;

import cn.com.constant.exception.ExceptionEnum;
import cn.com.constant.exception.FailedEnum;
import cn.com.constant.exception.WarnEnum;
import cn.com.entities.ErrorMailSendLogEntity;
import cn.com.exception.GlobalException;
import cn.com.exception.ServiceInternalException;
import cn.com.exception.UserIllegalOperationException;
import cn.com.mq.sender.ErrorEMailSender;
import cn.com.mq.threadpool.MessageScheduleThreadPool;
import cn.com.service.ErrorMailSendLogService;
import cn.com.vo.RespBean;
import cn.com.properties.GlobalEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Arrays;

/**
 * 全局异常控制(响应json)，区分 dev 环境和 prod 环境
 * @author wyl
 * @create 2020-08-05 09:10
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private GlobalEnvironment globalEnvironment;
    @Autowired
    private ErrorMailSendLogService errorMailSendLogService;
    @Autowired
    private ErrorEMailSender errorEMailSender;
    @Autowired
    private MessageScheduleThreadPool messageScheduleThreadPool;

    @ExceptionHandler(Exception.class)
    public Object globalExceptionHandlerDev(Exception e) {
        String profile = globalEnvironment.getProfile();

        // 认证的东西比较繁琐，东西也是一次性的，没有复用性，由认证流程决定信息
        if (e instanceof AuthenticationException) {
            return authenticationExceptionProcessing((AuthenticationException) e);
        }

        // 开发环境多打印一次控制台
        if (profile.equals(GlobalEnvironment.DEV)) {
            e.printStackTrace();
            return logProcessing(e);
        } else if (profile.equals(GlobalEnvironment.PROD)) {
            return logProcessing(e);
        } else {
            return logProcessing(e);
        }
    }

    private RespBean logProcessing(Exception e) {
        // sql 关联异常
        if (e instanceof SQLIntegrityConstraintViolationException) {
            if (log.isWarnEnabled()) {
                log.warn(FailedEnum.CORRELATION.getTip(), e);
            }
            return RespBean.error(FailedEnum.CORRELATION.getTip());
        }
        if (e instanceof ServiceInternalException || e instanceof UserIllegalOperationException) {
            GlobalException globalException = (GlobalException) e;
            // 错误
            if (globalException.getExceptionEnum().getClass().isAssignableFrom(FailedEnum.class)) {
                return errorProcessing(globalException.getExceptionEnum(), e);

            }
            // 警告
            else if (globalException.getExceptionEnum().getClass().isAssignableFrom(WarnEnum.class)) {
                return warnProcessing(globalException.getExceptionEnum(), e);
            }
        }

        if (log.isErrorEnabled()) {
            log.error(FailedEnum.VHR_DETAIL_TIPS.getTip(), e);
        }
        ErrorMailSendLogEntity errorMailSendLogEntity = new ErrorMailSendLogEntity();
        errorMailSendLogEntity.setBody(FailedEnum.VHR_DETAIL_TIPS.getTip() + " :" + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()) + "\n" + e.getCause());
        errorEMailSender.sendErrorEMailAndSaveLog(errorMailSendLogEntity);
        return RespBean.error(FailedEnum.VHR_DETAIL_TIPS.getTip(), e);
    }

    private RespBean authenticationExceptionProcessing(AuthenticationException e) {
        if (log.isTraceEnabled()) {
            log.trace(e.getMessage(), e);
        }
        return RespBean.error(e.getMessage());
    }

    private RespBean errorProcessing(ExceptionEnum exceptionEnum, Exception e) {
        FailedEnum failedEnum = (FailedEnum) exceptionEnum;
        if (log.isErrorEnabled()) {
            log.error(failedEnum.getTip(), e);
        }
        return RespBean.error(failedEnum.getTip());

    }

    private RespBean warnProcessing(ExceptionEnum exceptionEnum, Exception e) {
        WarnEnum failedEnum = (WarnEnum) exceptionEnum;
        if (log.isWarnEnabled()) {
            log.warn(failedEnum.getTip(), e);
        }
        return RespBean.error(failedEnum.getTip());
    }
}
