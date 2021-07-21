package cn.com.service.impl;

import cn.com.dao.EmployeeDao;
import cn.com.entities.DepartmentEntity;
import cn.com.entities.EmployeeEntity;
import cn.com.entities.PositionEntity;
import cn.com.entities.WelcomeMailLogEntity;
import cn.com.mapstruct.EmpEntity2VO;
import cn.com.mq.sender.WelcomeEMailSender;
import cn.com.service.DepartmentService;
import cn.com.service.EmployeeService;
import cn.com.service.PositionService;
import cn.com.util.VhrDateUtils;
import cn.com.util.VhrFuzzyQueryUtils;
import cn.com.vo.EmployeeEmailVO;
import cn.com.vo.RespPageBean;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
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
public class EmployeeServiceImpl extends ServiceImpl<EmployeeDao, EmployeeEntity> implements EmployeeService {

    @Autowired
    private EmployeeDao employeeDao;
    @Autowired
    private WelcomeEMailSender welcomeEMailSender;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private PositionService positionService;


    private final  DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy");
    private final  DateTimeFormatter mothFormatter = DateTimeFormatter.ofPattern("MM");

    @Override
    public RespPageBean getEmployeeByLimit(Integer offset, Integer size, EmployeeEntity employee, Date[] beginDateScope) {
        offset = (offset - 1) * size;
        VhrFuzzyQueryUtils.fuzzyProcessing(employee);
        List<List<?>> employeeByLimit = employeeDao.getEmployeeByLimit(offset, size, employee, VhrDateUtils.date2String(beginDateScope));
        return RespPageBean.processing(employeeByLimit);
    }

    @Override
    @Transactional(timeout = -1)
    public void addEmployee(EmployeeEntity employee) {
        LocalDate beginContract = employee.getBeginContract();
        LocalDate endContract = employee.getEndContract();
        BigDecimal year = new BigDecimal(this.yearFormatter.format(endContract)).subtract(new BigDecimal(this.yearFormatter.format(beginContract)));
        BigDecimal moth = new BigDecimal(this.mothFormatter.format(endContract)).subtract(new BigDecimal(this.mothFormatter.format(beginContract)));
        BigDecimal contractTerm = year.multiply(new BigDecimal("12")).add(moth);
        DecimalFormat decimalFormat = new DecimalFormat("##.00");
        employee.setContractTerm(Double.parseDouble(decimalFormat.format(contractTerm.doubleValue())));
        employeeDao.insert(employee);

        // 查询具体的属性
        DepartmentEntity departmentEntity = departmentService.getById(employee.getDepartmentId());
        PositionEntity positionEntity = positionService.getById(employee.getPosId());

        // 转换vo
        EmployeeEmailVO employeeEmailVO = EmpEntity2VO.INSTANCE.empEntity2VO(employee);
        employeeEmailVO.setDepartmentName(departmentEntity.getName());
        employeeEmailVO.setPosName(positionEntity.getName());
        // 发送邮件
        WelcomeMailLogEntity welcomeMailLogEntity = new WelcomeMailLogEntity();
        welcomeMailLogEntity.setEmpId(employee.getId());
        welcomeEMailSender.sendWelcomeEMailAndSaveLog(welcomeMailLogEntity, employeeEmailVO);
    }


    @Override
    public boolean batchAddEmployee(List<? extends EmployeeEntity> employeeList) {
        employeeDao.batchInsertEmployee(employeeList);
        return true;
    }


    @Override
    public int maxWorkID() {
        return employeeDao.maxWorkId();
    }
}
