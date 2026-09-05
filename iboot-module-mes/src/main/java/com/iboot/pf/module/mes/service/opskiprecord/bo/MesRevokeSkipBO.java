package com.iboot.pf.module.mes.service.opskiprecord.bo;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 撤销跳序参数。
 *
 * <p>入口是跳过记录而不是工序任务：一道工序可能被跳过、撤销、再跳过，撤销必须指明撤的是哪一次，
 * 否则第二次跳序的记录会被第一次的撤销动作带走。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MesRevokeSkipBO extends MesCommandReqDTO {

    /** 跳过记录编号，必填。 */
    private Long skipRecordId;

    /** 撤销原因，写进在制流水的 remark 与记录备注。 */
    private String reason;

}
