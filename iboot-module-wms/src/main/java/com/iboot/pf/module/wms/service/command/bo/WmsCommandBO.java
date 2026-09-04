package com.iboot.pf.module.wms.service.command.bo;

import com.iboot.pf.module.wms.api.dto.WmsCommandReqDTO;

/**
 * 命令幂等参数的模块内实现。
 *
 * <p>{@link WmsCommandReqDTO} 是给跨模块 API 当基类的抽象类，模块内部的动作（单据提交、确认、
 * 取消这些从界面发起的）没有对应的跨模块 DTO，用这个具体类填幂等键。
 */
public class WmsCommandBO extends WmsCommandReqDTO {

    /**
     * 按四段幂等键构造。
     *
     * @param idempotencyKey  幂等键，格式 sourceModule:sourceBizNo:action:sourceBizLineNo
     * @param sourceModule    来源模块，取值见 WmsSourceModuleEnum
     * @param sourceBizNo     来源业务单号
     * @param sourceBizLineNo 来源业务行号，整单命令传空
     */
    public static WmsCommandBO of(String idempotencyKey, String sourceModule,
                                  String sourceBizNo, String sourceBizLineNo) {
        WmsCommandBO bo = new WmsCommandBO();
        bo.setIdempotencyKey(idempotencyKey);
        bo.setSourceModule(sourceModule);
        bo.setSourceBizNo(sourceBizNo);
        bo.setSourceBizLineNo(sourceBizLineNo);
        return bo;
    }

}
