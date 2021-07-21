package cn.com.service.impl;

import cn.com.dao.PositionDao;
import cn.com.entities.PositionEntity;
import cn.com.exception.UserIllegalOperationException;
import cn.com.service.PositionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wyl
 * @since 2020-08-02
 */
@Service
public class PositionServiceImpl extends ServiceImpl<PositionDao, PositionEntity> implements PositionService {

    @Autowired
    private PositionDao positionDao;


    @Override
    public void deletePositionById(Integer id) {
        positionDao.deleteById(id);
    }

    @Override
    @Transactional
    public void deletePositionsByIds(Integer[] ids, int length) {
        int i = positionDao.deleteBatchIds(Arrays.asList(ids));
        if (i != length) {
            throw new UserIllegalOperationException("删除了不等于 " + length + "的数据，ids = " + Arrays.toString(ids));
        }
    }
}
