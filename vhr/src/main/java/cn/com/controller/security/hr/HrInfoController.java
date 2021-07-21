package cn.com.controller.security.hr;

import cn.com.bo.Hr;
import cn.com.constant.exception.FailedEnum;
import cn.com.constant.exception.SucceedEnum;
import cn.com.constant.exception.WarnEnum;
import cn.com.vo.RespBean;
import cn.com.entities.HrEntity;
import cn.com.exception.ServiceInternalException;
import cn.com.util.AuthenticationJudgeUtil;
import cn.com.service.HrService;
import cn.com.util.ImgUpLoadUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * @author wyl
 * @create 2020-08-03 19:48
 */
@Slf4j
@RestController
public class HrInfoController {
    @Autowired
    private HrService hrService;

    @GetMapping("/hr/info")
    public HrEntity getCurrentHr(Authentication authentication) {
        try {
            return (Hr) authentication.getPrincipal();
        } catch (Exception e) {
            throw new ServiceInternalException(FailedEnum.SELECT, e);
        }
    }

    @PutMapping("/hr/info")
    public RespBean updateHr(@RequestBody HrEntity hr, Authentication authentication) {
        try {
            hr.setId(AuthenticationJudgeUtil.getHrId());
            hrService.updateHr(hr);
            AuthenticationJudgeUtil.judgeAndSaveSecurityContext(authentication, hr);
            return RespBean.ok(SucceedEnum.UPDATE);
        } catch (Exception e) {
            throw new ServiceInternalException(FailedEnum.UPDATE, e);
        }
    }

    @PutMapping("/hr/pass")
    public RespBean updateHrPasswd(@RequestBody Map<String, Object> info, Authentication authentication) {
        try {
            String oldPass = (String) info.get("oldpass");
            String newPass = (String) info.get("pass");
            if (StringUtils.isEmpty(oldPass) || StringUtils.isEmpty(newPass)) {
                throw new ServiceInternalException(WarnEnum.DEFAULT, oldPass, newPass);
            }
            hrService.updateHrPasswd(oldPass, newPass, AuthenticationJudgeUtil.getHrId());
            return RespBean.ok(SucceedEnum.UPDATE);
        } catch (Exception e) {
            throw new ServiceInternalException(FailedEnum.UPDATE, e);
        }
    }

    @PostMapping("/hr/userface")
    public RespBean updateHrUserFace(MultipartFile file, @RequestParam("id") Integer id, Authentication authentication) {
        try {
            String fileUrl = null;
            try {
                fileUrl = ImgUpLoadUtil.upload(file.getBytes());
            } catch (IOException e) {
                throw new ServiceInternalException(FailedEnum.UPLOAD, e);
            }
            Hr hr = ((Hr) authentication.getPrincipal());
            hrService.updateUserFace(fileUrl, AuthenticationJudgeUtil.getHrId());
            hr.setUserface(fileUrl);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return RespBean.ok(SucceedEnum.UPLOAD);
        } catch (Exception e) {
            throw new ServiceInternalException(FailedEnum.UPLOAD, e);
        }
    }
}
