package com.iboot.pf.module.mes.service.proddemand.bo;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import lombok.Data;

/**
 * 生产需求确认与取消的动作参数。
 *
 * <p>两个动作共用一个参数对象：它们的入参完全相同（定位一条需求 + 一句可读说明），
 * 拆成两个类只会多一份要同步维护的字段清单。动作语义由调用的方法与
 * {@link com.iboot.pf.module.mes.enums.MesCommandTypeEnum} 区分，不靠参数类型区分。
 *
 * <p>继承 {@link MesCommandReqDTO} 拿到幂等键与来源三段，可以直接交给
 * {@link com.iboot.pf.module.mes.service.command.MesCommandService} 做重复下发拦截，不用再转一层载体。
 */
@Data
public class MesConfirmDemandBO extends MesCommandReqDTO {

    /** 生产需求编号，必填。 */
    private Long demandId;

    /**
     * 可读说明，可为空。
     *
     * <p>确认时记确认说明，取消时记取消原因，落到 {@code mes_prod_demand.remark}。
     * 取消原因是事后追查"这批需求为什么没投产"的唯一线索，界面上应当引导填写。
     */
    private String remark;

}
