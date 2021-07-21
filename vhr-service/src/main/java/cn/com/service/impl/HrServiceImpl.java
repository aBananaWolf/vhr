package cn.com.service.impl;

import cn.com.bo.HrBO;
import cn.com.dao.HrDao;
import cn.com.dao.HrRoleDao;
import cn.com.entities.HrEntity;
import cn.com.entities.HrRoleEntity;
import cn.com.exception.ServiceInternalException;
import cn.com.mapstruct.HrEntity2BO;
import cn.com.service.CacheSessionRegistryService;
import cn.com.service.HrService;
import cn.com.util.VhrFuzzyQueryUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wyl
 * @since 2020-08-02
 */
@Service
public class HrServiceImpl extends ServiceImpl<HrDao, HrEntity> implements HrService {
    @Autowired
    private HrDao hrDao;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private HrRoleDao hrRoleDao;

    @Autowired(required = false)
    private CacheSessionRegistryService sessionRegistry;

    @Override
    public List<HrBO> listHrAndRoleByHrBO(HrBO hr) {
        return hrDao.listHrAndRoleByHrBO(hr);
    }

    /**
     * {@link SessionControlConfig}
     * @param hr
     * @return
     */
    @Override
    public void updateHr(HrEntity hr) {
        HrEntity hrEntity = new HrEntity();
        hrEntity.setId(hr.getId());
        hrDao.update(hr, new QueryWrapper<>(hrEntity));
        // 判断管理员在禁用用户
        if (hr.getEnabled() != null && !hr.getEnabled()) {
            sessionRegistry.kickOutUser(HrEntity2BO.INSTANCE.hrEntity2BO(hr));
        }
    }

    /**
     * 可以考虑做一个 历史密码
     */
    @Override
    public void updateHrPasswd(String oldPass, String newPass, Integer hrId) {
        HrEntity temp = new HrEntity();
        temp.setId(hrId);
        QueryWrapper<HrEntity> hrEntityQueryWrapper = new QueryWrapper<>(temp);
        List<HrEntity> hrEntities = hrDao.selectList(hrEntityQueryWrapper);

        if (CollectionUtils.isEmpty(hrEntities)) {
            throw new ServiceInternalException("服务器内部数据错误！hrId没有查询到数据：" + hrEntities);
        }

        HrEntity hrEntity = hrEntities.get(0);
        if (!encoder.matches(oldPass,hrEntity.getPassword())) {
            throw new ServiceInternalException("旧密码输入不正确");
        }

        String encode = encoder.encode(newPass);

        hrEntity.setPassword(encode);
        hrDao.updateById(hrEntity);
    }

    @Override
    public void updateUserFace(String fileUrl, Integer hrId) {
        HrEntity hrEntity = new HrEntity();
        hrEntity.setUserface(fileUrl);
        hrEntity.setId(hrId);
        hrDao.updateById(hrEntity);
    }

    @Override
    public List<HrBO> getAllHrs(String keywords) {
        HrEntity hrEntity = new HrEntity();
        hrEntity.setName(keywords);
        VhrFuzzyQueryUtils.fuzzyProcessing(hrEntity);
        return hrDao.selectAllHrsWithRole(hrEntity);
    }

    @Override
    public boolean updateHrRole(Integer hrid, Integer[] rids) {
        HrRoleEntity hrRoleUdpEntity = new HrRoleEntity();
        hrRoleUdpEntity.setHrid(hrid);
        hrRoleDao.delete(new QueryWrapper<>(hrRoleUdpEntity));
        ArrayList<HrRoleEntity> list = new ArrayList<>(rids.length);
        for (int i = 0; i < rids.length; i++) {
            HrRoleEntity hrRole = new HrRoleEntity();
            hrRole.setHrid(hrid);
            hrRole.setRid(rids[i]);
            list.add(hrRole);
        }
        hrRoleDao.bachSave(list);
        return true;
    }

    @Override
    public boolean deleteHrById(Integer hrId) {
        HrRoleEntity hrRoleDelEntity = new HrRoleEntity();
        hrRoleDelEntity.setHrid(hrId);
        hrRoleDao.delete(new QueryWrapper<>(hrRoleDelEntity));
        hrDao.deleteById(hrId);
        return true;
    }
}
