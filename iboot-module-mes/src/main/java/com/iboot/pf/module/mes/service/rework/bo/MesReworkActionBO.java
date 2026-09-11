package com.iboot.pf.module.mes.service.rework.bo;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 返工任务状态动作参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MesReworkActionBO extends MesCommandReqDTO {

    private Long reworkId;
    private String reason;
}
