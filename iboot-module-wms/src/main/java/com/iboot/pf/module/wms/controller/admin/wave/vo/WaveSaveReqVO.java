package com.iboot.pf.module.wms.controller.admin.wave.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.iboot.pf.module.wms.dal.dataobject.waveline.WaveLineDO;
import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.wms.enums.WmsWaveStatusEnum;
import com.iboot.pf.module.wms.enums.WmsWaveTypeEnum;

@Schema(description = "管理后台 - WMS-出库波次新增/修改 Request VO")
@Data
public class WaveSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "波次编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "波次编号不能为空")
    private String waveNo;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "仓库编号不能为空")
    private Long whId;

    @Schema(description = "波次类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "波次类型不能为空")
    @InEnum(WmsWaveTypeEnum.class)
    private String waveType;

    @Schema(description = "计划开始时间")
    private LocalDateTime planStartTime;

    @Schema(description = "计划结束时间")
    private LocalDateTime planEndTime;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "状态不能为空")
    @InEnum(WmsWaveStatusEnum.class)
    private String status;

    @Schema(description = "备注")
    private String remark;

}
