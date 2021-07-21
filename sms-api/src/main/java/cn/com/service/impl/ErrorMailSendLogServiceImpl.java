package cn.com.service.impl;

import cn.com.dao.ErrorMailSendLogDao;
import cn.com.entities.ErrorMailSendLogEntity;
import cn.com.service.ErrorMailSendLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wyl
 * @since 2020-08-14
 */
@Service
public class ErrorMailSendLogServiceImpl extends ServiceImpl<ErrorMailSendLogDao, ErrorMailSendLogEntity> implements ErrorMailSendLogService {

    @Autowired
    private ErrorMailSendLogDao errorMailSendLogDao;

    @Override
    public List<ErrorMailSendLogEntity> findMessageByStatusAndTryTime(ErrorMailSendLogEntity errorMailSendLogEntity, Integer page, Integer size) {
        return errorMailSendLogDao.selectMessageByStatusAndTryTime(errorMailSendLogEntity,page,size);
    }

    @Override
    @Transactional
    public int idempotentInConsumers(ErrorMailSendLogEntity errorMailSendLogUDPEntity, Integer clientReceivedStatus) {
        return errorMailSendLogDao.idempotentInConsumers(errorMailSendLogUDPEntity, clientReceivedStatus);
    }
}
