package com.iboot.pf.module.aps.api.load.dto;

import com.iboot.pf.module.aps.api.dto.ApsCommandReqDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 刷新资源负荷命令。 */
@Data @EqualsAndHashCode(callSuper = true)
public class RefreshLoadReqDTO extends ApsCommandReqDTO {
    /** 排程版本编号。 */ @NotNull private Long versionId;
}
