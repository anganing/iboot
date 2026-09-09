package com.iboot.pf.module.mes.service.qctask.bo;

import com.iboot.pf.module.mes.service.qcrule.bo.QcTriggerContextBO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 规则驱动的质检任务创建参数。
 *
 * <p>五个检验点（IQC / FAI / IPQC / FQC / OQC）挂载在业务动作上，各自构造本 BO 调
 * {@code MesQcTaskDomainService.createQcTask}。检验方案由规则解析后锁定，调用方
 * <b>不带</b> qcPlanId：指定方案就绕过了规则，事后无法解释"这批为什么按这个方案检"，
 * 与 {@link com.iboot.pf.module.mes.api.quality.dto.CreateQcTaskReqDTO} 的约定同形。
 *
 * <p>送检数量与计量单位随本 BO 从 {@link QcTriggerContextBO} 继承的字段传递——
 * 上下文的描述性字段就是为生成任务准备的快照来源，数量不在本 BO 重复出现，避免两处取值互相打架。
 *
 * @author iboot
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MesCreateQcTaskBO extends QcTriggerContextBO {

    /** 触发点，取值 {@link com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum}，必填。 */
    @NotBlank(message = "触发点不能为空")
    private String triggerPoint;

    /** 备注。超过空行上限降级生成时，系统标注会追加在它后面。 */
    private String remark;

}
