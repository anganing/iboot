package com.iboot.pf.module.aps.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** APS 跨模块命令公共参数。 */
@Data
public class ApsCommandReqDTO {

    /** 全局唯一幂等键，建议格式：sourceModule:sourceBizNo:action。 */
    @NotBlank(message = "幂等键不能为空")
    private String idempotencyKey;
    /** 来源模块：MES、APS、INTEGRATION、UI。 */
    @NotBlank(message = "来源模块不能为空")
    private String sourceModule;
    /** 来源业务单号。 */
    @NotBlank(message = "来源业务单号不能为空")
    private String sourceBizNo;
    /** 来源业务行号。 */
    private String sourceBizLineNo;
}
