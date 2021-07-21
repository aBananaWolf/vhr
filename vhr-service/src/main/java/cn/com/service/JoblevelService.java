package cn.com.service;

import cn.com.entities.JoblevelEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wyl
 * @since 2020-08-02
 */
public interface JoblevelService extends IService<JoblevelEntity> {
    void deleteJobLevelById(Integer id);

    void deleteJobLevelsByIds(Integer[] ids, int length);
}
