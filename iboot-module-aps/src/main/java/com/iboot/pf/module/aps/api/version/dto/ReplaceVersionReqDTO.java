package com.iboot.pf.module.aps.api.version.dto;

import com.iboot.pf.module.aps.api.dto.ApsCommandReqDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 替代已发布排程版本命令。 */
@Data @EqualsAndHashCode(callSuper = true)
public class ReplaceVersionReqDTO extends ApsCommandReqDTO {
    /** 被替代版本编号。 */ @NotNull private Long oldVersionId;
    /** 新版本编号。 */ @NotNull private Long newVersionId;
    /** 操作人员编号。 */ private Long operatorId;
}
