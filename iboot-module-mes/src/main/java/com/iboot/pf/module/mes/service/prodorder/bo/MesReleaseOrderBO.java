package com.iboot.pf.module.mes.service.prodorder.bo;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 生产工单下达参数。
 *
 * <p>下达是 MES 最重的一个动作：锁 BOM 与工艺路线版本、按路线展开作业工单与工序任务、串工序前后序。
 * 它没有新编号可返回，因此继承 {@link MesCommandReqDTO} 走
 * {@link com.iboot.pf.module.mes.service.command.MesCommandService#executeVoid} 判重。
 * 幂等键留空时由服务端按 {@code UI:{orderNo}:RELEASE_PROD_ORDER:0} 派生，界面连点两次或上游重试
 * 只展开一批工序任务。
 *
 * <p>这里**故意不提供版本或生效日期入参**：下达锁的必须是"下达那一刻"物料的生效版本，
 * 允许调用方指定就等于允许锁一个不生效的版本，基石"在制工单用下达时的版本"当场失效。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MesReleaseOrderBO extends MesCommandReqDTO {

    /** 生产工单编号，必填。 */
    private Long prodOrderId;

    /** 下达备注，写进在制流水的 remark，可为空。 */
    private String remark;

}
