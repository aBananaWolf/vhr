package cn.com.service;

import cn.com.entities.ErrorMailSendLogEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wyl
 * @since 2020-08-14
 */
public interface ErrorMailSendLogService extends IService<ErrorMailSendLogEntity> {

    List<ErrorMailSendLogEntity> findMessageByStatusAndTryTime(ErrorMailSendLogEntity errorMailSendLogEntity, Integer page, Integer size);

    int idempotentInConsumers(ErrorMailSendLogEntity errorMailSendLogUDPEntity, Integer clientReceivedStatus);

 }
