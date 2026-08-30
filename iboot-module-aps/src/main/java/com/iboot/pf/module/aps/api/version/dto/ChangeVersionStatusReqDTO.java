package com.iboot.pf.module.aps.api.version.dto;

import com.iboot.pf.module.aps.api.dto.ApsCommandReqDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 排程版本状态命令。 */
@Data @EqualsAndHashCode(callSuper = true)
public class ChangeVersionStatusReqDTO extends ApsCommandReqDTO {
    /** 版本编号。 */ @NotNull private Long versionId;
    /** 操作人员编号。 */ private Long operatorId;
}
