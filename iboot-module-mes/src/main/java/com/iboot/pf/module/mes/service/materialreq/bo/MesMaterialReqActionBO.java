package com.iboot.pf.module.mes.service.materialreq.bo;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 领退补料申请状态动作参数，提交与取消共用。
 *
 * <p>**目标状态不由调用方指定**：动作名决定目标状态，能不能迁移过去由服务端按当前状态裁决。
 * 入参带目标状态就等于让调用方绕过预留与释放这两步直接改状态。
 *
 * <p>两个动作都是状态推进型、没有新编号可返回，走
 * {@link com.iboot.pf.module.mes.service.command.MesCommandService} 判重。幂等键留空时按
 * {@code {来源模块}:{申请单号}:{命令类型}:0} 派生，例如 {@code UI:MR-0001:SUBMIT_MATERIAL_REQ:0}。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MesMaterialReqActionBO extends MesCommandReqDTO {

    /** 领退补料申请编号，必填。 */
    private Long materialReqId;

    /** 动作原因，写进申请备注；取消时建议填写。 */
    private String reason;

}
