package cn.com.service.impl;

import cn.com.constant.message.MessageConstants;
import cn.com.entities.ErrorMailSendLogEntity;
import cn.com.service.ErrorMailSendLogService;
import cn.com.service.SendErrorMailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * @author wyl
 * @create 2020-08-15 14:46
 */
@Service
public class SendErrorMailServiceImpl implements SendErrorMailService {
    @Autowired
    private ErrorMailSendLogService errorMailSendLogService;
    @Autowired
    private JavaMailSenderImpl javaMailSender;

    @Transactional(timeout = -1)
    public void sendErrorMail(ErrorMailSendLogEntity errorMailSendLogUDPEntity, int theClientReceivedIt) throws MessagingException {
        // 保障消息只会接收一次, 1 发送成功 -> 4 消费成功，重复的消息无法再根据 1 的状态进行修改
        int affected = errorMailSendLogService.idempotentInConsumers(errorMailSendLogUDPEntity, MessageConstants.CONSUME_SUCCESSFUL);
        if (affected != 1) {
            // 走入这里说明是重复消息
            return;
        }
        // 查询消息体
        ErrorMailSendLogEntity errorMessageBody = errorMailSendLogService.getById(errorMailSendLogUDPEntity.getMsgId());
        // 成功标志
//        errorMailSendLogUDPEntity.setStatus(MessageConstants.CONSUME_SUCCESSFUL);
//        errorMailSendLogService.updateById(errorMailSendLogUDPEntity);
        // 比较耗时的操作，放在事务中，事务超时设置为-1
        // 如果上面的操作没有错误，会在这里一直慢慢的发送邮件
        // 如果邮件发送错误，上面的操作就会回滚
        this.sendErrorMail(errorMessageBody.getMsgId());
    }

    private void sendErrorMail(String id) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setSubject("开饭通知");
        mimeMessageHelper.setText("详细异常id：" + id + " 请及时查看数据库", true);
        mimeMessageHelper.setFrom("13049394389@163.com");
        mimeMessageHelper.setTo("abananawolf@qq.com");
        javaMailSender.send(mimeMessage);
    }
}
