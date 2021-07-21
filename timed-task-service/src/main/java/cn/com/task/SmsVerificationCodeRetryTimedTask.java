package cn.com.task;

import cn.com.constant.message.MessageConstants;
import cn.com.constant.message.SmsCodeConstants;
import cn.com.entities.SmsVerificationCodeSendLogEntity;
import cn.com.mq.sender.SmsCodeSender;
import cn.com.service.SmsVerificationCodeSendLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

import static cn.com.constant.TaskShardingItemEnum.*;

/**
 * @author wyl
 * @create 2020-08-18 09:17
 */
@Slf4j
@Component
public class SmsVerificationCodeRetryTimedTask implements SimpleJob {
    @Autowired
    private SmsVerificationCodeSendLogService smsVerificationCodeSendLogService;
    @Autowired
    private SmsCodeSender smsCodeSender;

    @Override
    public void execute(ShardingContext shardingContext) {
        int shardingItem = shardingContext.getShardingItem();
        switch (get(shardingItem)) {
            case Shenzheng:
                this.attemptSendSmsCode();
                break;
        }
    }

    private void attemptSendSmsCode() {
        SmsVerificationCodeSendLogEntity smsVerificationCodeSendLogEntity = new SmsVerificationCodeSendLogEntity();
        LocalDateTime now = LocalDateTime.now();
        smsVerificationCodeSendLogEntity.setUpdateTime(now);
        smsVerificationCodeSendLogEntity.setTryTime(now);
        smsVerificationCodeSendLogEntity.setStatus(MessageConstants.SENDING);

        int page = 0;
        int size = 2;
        List<SmsVerificationCodeSendLogEntity> list = null;
        while (true) {
            try {
                list = smsVerificationCodeSendLogService.findMessageByStatusAndTryTime(smsVerificationCodeSendLogEntity, page, size);
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("定时任务查询数据库异常", e);
                }
                return;
            }
            if (CollectionUtils.isEmpty(list)) {
                return;
            } else {
                // 也可以一直查询第一页，后面的操作会设置失败状态
                page++;
            }

            for (SmsVerificationCodeSendLogEntity verificationCodeSendLogEntity : list) {
                try {
                    if (verificationCodeSendLogEntity.getAttemptsCount() < SmsCodeConstants.SMS_CODE_SENT_RETRY_COUNT) {
                        verificationCodeSendLogEntity.setAttemptsCount(verificationCodeSendLogEntity.getAttemptsCount() + 1);
                        smsVerificationCodeSendLogService.updateById(verificationCodeSendLogEntity);
                    } else {
                        verificationCodeSendLogEntity.setStatus(MessageConstants.SENDING);
                        int affected = smsVerificationCodeSendLogService.idempotentInConsumers(verificationCodeSendLogEntity, MessageConstants.FAIL_IN_SEND);
                        if (affected != 1) {
                            continue;
                        }
                        if (log.isErrorEnabled()) {
                            log.error("验证码发送失败，消息已达到最大重试次数：" + verificationCodeSendLogEntity);
                        }
                        continue;
                    }
                    // 异常测试
                    // int a = 1 / 0;
                    smsCodeSender.sendSmsVerificationCodeAndUpdateLog(verificationCodeSendLogEntity);
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error("定时任务重试发送消息时出现异常", e);
                    }
                }
            }
        }
    }
}
