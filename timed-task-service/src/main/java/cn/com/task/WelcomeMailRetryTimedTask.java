package cn.com.task;

import cn.com.constant.message.EMailConstants;
import cn.com.constant.message.MessageConstants;
import cn.com.entities.DepartmentEntity;
import cn.com.entities.EmployeeEntity;
import cn.com.entities.PositionEntity;
import cn.com.entities.WelcomeMailLogEntity;
import cn.com.mapstruct.EmpEntity2VO;
import cn.com.mq.sender.WelcomeEMailSender;
import cn.com.service.DepartmentService;
import cn.com.service.EmployeeService;
import cn.com.service.PositionService;
import cn.com.service.WelcomeMailLogService;
import cn.com.vo.EmployeeEmailVO;
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
 * @create 2020-08-18 16:48
 */
@Slf4j
@Component
public class WelcomeMailRetryTimedTask implements SimpleJob {
    @Autowired
    private WelcomeMailLogService welcomeMailLogService;
    @Autowired
    private WelcomeEMailSender welcomeEMailSender;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private PositionService positionService;

    @Override
    public void execute(ShardingContext shardingContext) {
        int shardingItem = shardingContext.getShardingItem();
        switch (get(shardingItem)) {
            case Shenzheng:
                this.attemptSendWelComeMail();
                break;
        }
    }

    private void attemptSendWelComeMail() {
        WelcomeMailLogEntity welcomeMailLogEntity = new WelcomeMailLogEntity();
        welcomeMailLogEntity.setStatus(MessageConstants.SENDING);
        welcomeMailLogEntity.setTryTime(LocalDateTime.now());

        int page = 0;
        int size = 2;
        List<WelcomeMailLogEntity> welcomeMailLogEntities = null;

        while (true) {
            try {
                welcomeMailLogEntities = welcomeMailLogService.findMessageByStatusAndTryTime(welcomeMailLogEntity, page, size);
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("定时任务查询数据库异常", e);
                }
                return;
            }

            if (CollectionUtils.isEmpty(welcomeMailLogEntities)) {
                return;
            } else {
                // 也可以一直查询第一页，后面的操作会设置失败状态
                page++;
            }
            for (WelcomeMailLogEntity mailLogEntity : welcomeMailLogEntities) {
                try {
                    if (mailLogEntity.getAttemptCount() < EMailConstants.EMAIL_SENT_RETRY_COUNT) {
                        mailLogEntity.setAttemptCount(mailLogEntity.getAttemptCount() + 1);
                        welcomeMailLogService.updateById(mailLogEntity);
                    } else {
                        mailLogEntity.setStatus(MessageConstants.SENDING);
                        int affected = welcomeMailLogService.idempotentInConsumers(mailLogEntity, MessageConstants.FAIL_IN_SEND);
                        if (affected != 1) {
                            continue;
                        }
                        if (log.isErrorEnabled()) {
                            log.error("入职欢迎邮件发送失败，消息重试已达到最大重试次数：" + mailLogEntity);
                        }
                        continue;
                    }
                    // 异常测试
//                    int a = 1 / 0;
                    EmployeeEntity emp = employeeService.getById(mailLogEntity.getEmpId());
                    DepartmentEntity departmentEntity = departmentService.getById(emp.getDepartmentId());
                    PositionEntity positionEntity = positionService.getById(emp.getPosId());

                    EmployeeEmailVO employeeEmailVO = EmpEntity2VO.INSTANCE.empEntity2VO(emp);
                    employeeEmailVO.setDepartmentName(departmentEntity.getName());
                    employeeEmailVO.setPosName(positionEntity.getName());

                    welcomeEMailSender.sendWelcomeEMailAndUpdateLog(mailLogEntity, employeeEmailVO);
                } catch (Exception e) {
                    log.warn("定时任务尝试发送消息失败", e);
                }
            }
        }
    }
}

