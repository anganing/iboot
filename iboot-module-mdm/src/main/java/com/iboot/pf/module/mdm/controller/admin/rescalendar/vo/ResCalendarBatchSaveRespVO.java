package com.iboot.pf.module.mdm.controller.admin.rescalendar.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 资源日历批量排班 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResCalendarBatchSaveRespVO {

    @Schema(description = "新增条数", requiredMode = Schema.RequiredMode.REQUIRED, example = "120")
    private Integer createCount;

    @Schema(description = "覆盖更新条数", requiredMode = Schema.RequiredMode.REQUIRED, example = "8")
    private Integer updateCount;

}
