package cn.com.service;

import cn.com.bo.HrBO;
import cn.com.entities.HrEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wyl
 * @since 2020-08-02
 */
public interface HrService extends IService<HrEntity> {


    List<HrBO> listHrAndRoleByHrBO(HrBO hr);

    void updateHr(HrEntity hr);

    void updateHrPasswd(String oldPass, String newPass, Integer hrId);

    void updateUserFace(String fileUrl, Integer hrId);

    List<HrBO> getAllHrs(String keywords);

    boolean updateHrRole(Integer hrid, Integer[] rids);

    boolean deleteHrById(Integer id);
}
