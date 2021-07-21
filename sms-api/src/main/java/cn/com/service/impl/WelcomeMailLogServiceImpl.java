package cn.com.service.impl;

import cn.com.dao.WelcomeMailLogDao;
import cn.com.entities.WelcomeMailLogEntity;
import cn.com.service.WelcomeMailLogService;
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
 * @since 2020-08-18
 */
@Service
public class WelcomeMailLogServiceImpl extends ServiceImpl<WelcomeMailLogDao, WelcomeMailLogEntity> implements WelcomeMailLogService {

    @Autowired
    private WelcomeMailLogDao welcomeMailLogDao;

    @Override
    public int idempotentInConsumers(WelcomeMailLogEntity welcomeMailLogEntity, Integer clientReceivedStatus) {
        return welcomeMailLogDao.idempotentInConsumers(welcomeMailLogEntity, clientReceivedStatus);
    }

    @Override
    public List<WelcomeMailLogEntity> findMessageByStatusAndTryTime(WelcomeMailLogEntity welcomeMailLogEntity, Integer page, Integer size) {
        return welcomeMailLogDao.findMessageByStatusAndTryTime(welcomeMailLogEntity, page, size);
    }
}
