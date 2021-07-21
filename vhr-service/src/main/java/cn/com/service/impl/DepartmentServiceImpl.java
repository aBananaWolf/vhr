package cn.com.service.impl;

import cn.com.bo.DepartmentBO;
import cn.com.dao.DepartmentDao;
import cn.com.entities.DepartmentEntity;
import cn.com.service.DepartmentService;
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
 * @since 2020-08-02
 */
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentDao, DepartmentEntity> implements DepartmentService {

    @Autowired
    private DepartmentDao departmentDao;

    @Override
    public List<DepartmentBO> getAllDepartments() {
        // 从股东会为源递归查询
        return departmentDao.selectAllDepartments(-1);
    }

    @Override
    @Transactional
    public void insertDepartment(DepartmentBO dep) {
        dep.setEnabled(true);
        dep.setParent(false);
        departmentDao.insertDepartment(dep);
    }

    @Override
    @Transactional
    public void deleteDepartmentById(DepartmentBO dep) {
        departmentDao.deleteDepartmentById(dep);
    }
}
