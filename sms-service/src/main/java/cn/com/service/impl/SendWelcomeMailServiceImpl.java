package cn.com.service.impl;

import cn.com.constant.message.MessageConstants;
import cn.com.entities.EmployeeEntity;
import cn.com.entities.WelcomeMailLogEntity;
import cn.com.service.SendWelcomeMailService;
import cn.com.service.WelcomeMailLogService;
import cn.com.vo.EmployeeEmailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * @author wyl
 * @create 2020-08-19 14:51
 */
@Component
public class SendWelcomeMailServiceImpl implements SendWelcomeMailService {
    @Autowired
    private WelcomeMailLogService welcomeMailLogService;
    @Autowired
    private JavaMailSenderImpl javaMailSender;

    @Autowired
    TemplateEngine engine;

    @Override
    @Transactional(timeout = -1)
    public void sendWelcomeMail(WelcomeMailLogEntity welcomeMailLogEntity, EmployeeEmailVO employeeEntity) throws MessagingException {
        int affected = welcomeMailLogService.idempotentInConsumers(welcomeMailLogEntity, MessageConstants.CONSUME_SUCCESSFUL);
        if (affected != 1) {
            return;
        }
        this.sendErrorMail(employeeEntity);
    }
    private void sendErrorMail(EmployeeEmailVO emp) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setSubject("入职通知");
        Context context = new Context();
        context.setVariable("emp", emp);
        String process = engine.process("EmpEmail.html", context);
        mimeMessageHelper.setText(process, true);
        mimeMessageHelper.setFrom("13049394389@163.com");
        mimeMessageHelper.setTo(emp.getEmail());
        javaMailSender.send(mimeMessage);

    }
}
