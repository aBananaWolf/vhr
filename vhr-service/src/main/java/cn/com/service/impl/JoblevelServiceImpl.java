package cn.com.service.impl;

import cn.com.dao.JoblevelDao;
import cn.com.entities.JoblevelEntity;
import cn.com.exception.UserIllegalOperationException;
import cn.com.service.JoblevelService;
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
public class JoblevelServiceImpl extends ServiceImpl<JoblevelDao, JoblevelEntity> implements JoblevelService {
    @Autowired
    private JoblevelDao joblevelDao;

    @Override
    public void deleteJobLevelById(Integer id) {
        joblevelDao.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteJobLevelsByIds(Integer[] ids, int length) {
        int i = joblevelDao.deleteBatchIds(Arrays.asList(ids));
        if (i != length) {
            throw new UserIllegalOperationException("删除了不等于 " + length + "的数据，ids = " + Arrays.toString(ids));
        }
    }
}
