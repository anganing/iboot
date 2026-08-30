package com.iboot.pf.module.aps.api.projection.dto;

import com.iboot.pf.module.aps.api.dto.ApsCommandReqDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Collection;

/** 刷新 APS 投影命令。 */
@Data @EqualsAndHashCode(callSuper = true)
public class RefreshProjectionReqDTO extends ApsCommandReqDTO {
    /** 场景编号。 */ @NotNull private Long scenarioId;
    /** 物料投影刷新范围；刷新物料投影时必填。 */ private Collection<Long> materialIds;
}
