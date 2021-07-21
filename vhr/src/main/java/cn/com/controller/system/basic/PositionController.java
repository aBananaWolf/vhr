package cn.com.controller.system.basic;


import cn.com.constant.exception.FailedEnum;
import cn.com.constant.exception.SucceedEnum;
import cn.com.entities.PositionEntity;
import cn.com.exception.ServiceInternalException;
import cn.com.service.PositionService;
import cn.com.vo.RespBean;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
@RestController
@RequestMapping("/system/basic/pos")
public class PositionController {
    @Autowired
    private PositionService positionService;

    @GetMapping("/")
    public List<PositionEntity> getAllPositions() {
        try {
            return positionService.list();
        } catch(Exception e) {
            throw new ServiceInternalException(FailedEnum.SELECT, e);
        }
    }

    @PostMapping("/")
    public RespBean addPosition(@RequestBody PositionEntity position) {
        try {
            positionService.save(position);
            return RespBean.ok(SucceedEnum.INSERT);
        } catch(Exception e) {
            throw new ServiceInternalException(FailedEnum.INSERT, e);
        }
    }

    @PutMapping("/")
    public RespBean updatePositions(@RequestBody PositionEntity position) {
        try {
            PositionEntity positionUpdEntity = new PositionEntity();
            positionUpdEntity.setId(position.getId());
            QueryWrapper<PositionEntity> queryWrapper = new QueryWrapper<>(positionUpdEntity);
            if (position.getId() == null) {
                throw new ServiceInternalException(FailedEnum.DEFAULT,position);
            }
            positionService.update(position,queryWrapper);
            return RespBean.ok(SucceedEnum.UPDATE);
        } catch(Exception e) {
            throw new ServiceInternalException(FailedEnum.UPDATE, e);
        }
    }

    @DeleteMapping("/{id}")
    public RespBean deletePositionById(@PathVariable Integer id) {
        try {
            positionService.deletePositionById(id);
            return RespBean.ok(SucceedEnum.DELETE);
        } catch(Exception e) {
            throw new ServiceInternalException(FailedEnum.DELETE, e);
        }
    }

    @DeleteMapping("/")
    public RespBean deletePositionsByIds(Integer[] ids) {
        try {
            if (ArrayUtils.isEmpty(ids)) {
                throw new ServiceInternalException(FailedEnum.DEFAULT,ids);
            }
            positionService.deletePositionsByIds(ids,ids.length);
            return RespBean.ok(SucceedEnum.DELETE);
        } catch(Exception e) {
            throw new ServiceInternalException(FailedEnum.DELETE, e);
        }
    }
}

