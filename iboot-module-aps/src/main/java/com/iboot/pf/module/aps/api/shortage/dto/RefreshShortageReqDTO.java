package com.iboot.pf.module.aps.api.shortage.dto;

import com.iboot.pf.module.aps.api.dto.ApsCommandReqDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 刷新缺料汇总命令。 */
@Data @EqualsAndHashCode(callSuper = true)
public class RefreshShortageReqDTO extends ApsCommandReqDTO {
    /** 排程版本编号。 */ @NotNull private Long versionId;
}
