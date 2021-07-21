package cn.com.service.impl;

import cn.com.dao.SmsVerificationCodeSendLogDao;
import cn.com.entities.SmsVerificationCodeSendLogEntity;
import cn.com.service.SmsVerificationCodeSendLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wyl
 * @since 2020-08-17
 */
@Service
public class SmsVerificationCodeSendLogServiceImpl extends ServiceImpl<SmsVerificationCodeSendLogDao, SmsVerificationCodeSendLogEntity> implements SmsVerificationCodeSendLogService {

    @Autowired
    private SmsVerificationCodeSendLogDao smsVerificationCodeSendLogDao;

    @Override
    public int idempotentInConsumers(SmsVerificationCodeSendLogEntity smsVerificationCodeSendLogEntity, Integer clientReceivedStatus) {
        return smsVerificationCodeSendLogDao.idempotentInConsumers(smsVerificationCodeSendLogEntity, clientReceivedStatus);
    }

    @Override
    public List<SmsVerificationCodeSendLogEntity> findMessageByStatusAndTryTime(SmsVerificationCodeSendLogEntity smsVerificationCodeSendLogEntity, Integer page, Integer size) {
        return smsVerificationCodeSendLogDao.findMessageByStatusAndTryTime(smsVerificationCodeSendLogEntity, page, size);
    }
}
