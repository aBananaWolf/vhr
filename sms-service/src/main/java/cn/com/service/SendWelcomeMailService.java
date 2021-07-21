package cn.com.service;

import cn.com.entities.WelcomeMailLogEntity;
import cn.com.vo.EmployeeEmailVO;

import javax.mail.MessagingException;

public interface SendWelcomeMailService {
    void sendWelcomeMail(WelcomeMailLogEntity welcomeMailLogEntity, EmployeeEmailVO employeeEntity) throws MessagingException;
}
