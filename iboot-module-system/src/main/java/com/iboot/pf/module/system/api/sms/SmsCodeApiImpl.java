package com.iboot.pf.module.system.api.sms;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.system.api.sms.dto.code.SmsCodeValidateReqDTO;
import com.iboot.pf.module.system.api.sms.dto.code.SmsCodeSendReqDTO;
import com.iboot.pf.module.system.api.sms.dto.code.SmsCodeUseReqDTO;
import com.iboot.pf.module.system.service.sms.SmsCodeService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

/**
 * 短信验证码 API 实现类
 *
 * @author iboot
 */
@Service
@Validated
@DataPermission(enable = false)
public class SmsCodeApiImpl implements SmsCodeApi {

    @Resource
    private SmsCodeService smsCodeService;

    @Override
    public void sendSmsCode(SmsCodeSendReqDTO reqDTO) {
        smsCodeService.sendSmsCode(reqDTO);
    }

    @Override
    public void useSmsCode(SmsCodeUseReqDTO reqDTO) {
        smsCodeService.useSmsCode(reqDTO);
    }

    @Override
    public void validateSmsCode(SmsCodeValidateReqDTO reqDTO) {
        smsCodeService.validateSmsCode(reqDTO);
    }

}
