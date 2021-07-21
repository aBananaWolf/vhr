package cn.com.service;

import cn.com.entities.ErrorMailSendLogEntity;

import javax.mail.MessagingException;

public interface SendErrorMailService {
    void sendErrorMail(ErrorMailSendLogEntity errorMailSendLogUDPEntity, int theClientReceivedIt) throws MessagingException;
}
