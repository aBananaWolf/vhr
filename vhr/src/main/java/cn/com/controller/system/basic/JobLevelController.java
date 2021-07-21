package cn.com.controller.system.basic;


import cn.com.constant.exception.FailedEnum;
import cn.com.constant.exception.SucceedEnum;
import cn.com.entities.JoblevelEntity;
import cn.com.exception.ServiceInternalException;
import cn.com.service.JoblevelService;
import cn.com.vo.RespBean;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author wyl
 * @since 2020-08-02
 */
@Slf4j
@RestController
@RequestMapping("/system/basic/joblevel")
public class JobLevelController {

    @Autowired
    private JoblevelService jobLevelService;

    @GetMapping("/")
    public List<JoblevelEntity> getAllJobLevels() {
        try {
            return jobLevelService.list();
        } catch(Exception e) {
            throw new ServiceInternalException(FailedEnum.SELECT, e);
        }
    }

    @PostMapping("/")
    public RespBean addJobLevel(@RequestBody JoblevelEntity jobLevel) {
        try {
            jobLevelService.save(jobLevel);
            return RespBean.ok(SucceedEnum.INSERT);
        } catch(Exception e) {
            throw new ServiceInternalException(FailedEnum.INSERT, e);
        }
    }

    @PutMapping("/")
    public RespBean updateJobLevelById(@RequestBody JoblevelEntity jobLevel) {
        try {
            if (jobLevel.getId() == null) {
                throw new ServiceInternalException(FailedEnum.UPDATE, jobLevel);
            }
            JoblevelEntity jobLevelUpdEntity = new JoblevelEntity();
            jobLevelUpdEntity.setId(jobLevel.getId());
            jobLevelService.update(jobLevel, new QueryWrapper<>(jobLevelUpdEntity));
            return RespBean.ok(SucceedEnum.UPDATE);
        } catch(Exception e) {
            throw new ServiceInternalException(FailedEnum.UPDATE, e);
        }
    }

    @DeleteMapping("/{id}")
    public RespBean deleteJobLevelById(@PathVariable Integer id) {
        try {
            jobLevelService.deleteJobLevelById(id);
            return RespBean.ok(SucceedEnum.DELETE);
        } catch(Exception e) {
            throw new ServiceInternalException(FailedEnum.DELETE, e);
        }
    }

    @DeleteMapping("/")
    public RespBean deleteJobLevelsByIds(Integer[] ids) {
        try {
            if (ArrayUtils.isEmpty(ids)) {
                throw new ServiceInternalException(FailedEnum.DELETE, ids);
            }
            jobLevelService.deleteJobLevelsByIds(ids, ids.length);
            return RespBean.ok(SucceedEnum.DELETE);
        } catch(Exception e) {
            throw new ServiceInternalException(FailedEnum.DELETE, e);
        }
    }
}

