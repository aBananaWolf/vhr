package cn.com.service;

import cn.com.entities.WelcomeMailLogEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wyl
 * @since 2020-08-18
 */
public interface WelcomeMailLogService extends IService<WelcomeMailLogEntity> {

    int idempotentInConsumers(WelcomeMailLogEntity welcomeMailLogEntity, Integer clientReceivedStatus);

    List<WelcomeMailLogEntity> findMessageByStatusAndTryTime(WelcomeMailLogEntity welcomeMailLogEntity, Integer page, Integer size);
}
