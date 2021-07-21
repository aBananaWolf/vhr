package cn.com.task;

import cn.com.constant.message.EMailConstants;
import cn.com.constant.message.MessageConstants;
import cn.com.constant.TaskShardingItemEnum;
import cn.com.entities.ErrorMailSendLogEntity;
import cn.com.mq.sender.ErrorEMailSender;
import cn.com.service.ErrorMailSendLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author wyl
 * @create 2020-08-16 19:54
 */
@Slf4j
@Component
public class ErrorEmailRetryTimedTask implements SimpleJob {
    @Autowired
    private ErrorMailSendLogService errorMailSendLogService;
    @Autowired
    private ErrorEMailSender errorEMailSender;

    @Override
    public void execute(ShardingContext shardingContext) {
        int shardingItem = shardingContext.getShardingItem();
        switch (TaskShardingItemEnum.get(shardingItem)) {
            case Shenzheng:
                attemptSendErrorEMail();
                break;
        }
    }

    private void attemptSendErrorEMail() {
        ErrorMailSendLogEntity errorMailSendLogEntity = new ErrorMailSendLogEntity();
        errorMailSendLogEntity.setStatus(MessageConstants.SENDING);
        errorMailSendLogEntity.setTryTime(LocalDateTime.now());
        // 查询出没有成功发送的log
        int size = 2;
        int page = 0;
        List<ErrorMailSendLogEntity> errorMailList = null;

        while (true) {
            try {
                errorMailList = errorMailSendLogService.findMessageByStatusAndTryTime(errorMailSendLogEntity, page, size);
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("定时任务查询数据库异常", e);
                }
                return;
            }
            if (CollectionUtils.isEmpty(errorMailList)) {
                return;
            } else {
                // 也可以一直查询第一页，后面的操作会设置失败状态
                page++;
            }
            for (ErrorMailSendLogEntity mailSendLogEntity : errorMailList) {
                try {
                    // 重试次数 + 1
                    if (mailSendLogEntity.getCount() < EMailConstants.EMAIL_SENT_RETRY_COUNT) {
                        mailSendLogEntity.setCount(mailSendLogEntity.getCount() + 1);
                        errorMailSendLogService.updateById(mailSendLogEntity);
                    } else {
                        mailSendLogEntity.setStatus(MessageConstants.SENDING);
                        int affected = errorMailSendLogService.idempotentInConsumers(mailSendLogEntity, MessageConstants.FAIL_IN_SEND);
                        if (affected != 1) {
                            continue;
                        }
                        if (log.isErrorEnabled()) {
                            log.error("电子邮件发送失败，重试次数达到最大次数 " + mailSendLogEntity.getMsgId());
                        }
                        continue;
                    }
                    errorEMailSender.sendErrorEMailAndUpdateLog(mailSendLogEntity);
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error("定时任务重试发送消息时出现异常", e);
                    }
                }
            }
        }
    }
}
