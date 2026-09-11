package com.iboot.pf.module.mes.service.disposition.bo;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 处置单提交、执行、取消动作参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MesDispositionActionBO extends MesCommandReqDTO {

    @NotNull(message = "处置单编号不能为空")
    private Long dispositionId;
}
