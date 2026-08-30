package com.iboot.pf.module.mdm.api.route.dto;

import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 工艺路线详情 Response DTO
 *
 * @author iboot
 */
@Data
public class RouteDetailRespDTO {

    /** 工艺路线编号 */
    private Long id;

    /** 物料编号 */
    private Long matId;

    /** 版本号 */
    private String version;

    /** 状态：0-启用，1-停用 */
    private Integer status;

    /** 生效日期 */
    private LocalDate effectiveDate;

    /** 失效日期 */
    private LocalDate expiryDate;

    /** 工艺路线工序列表（按 seq 升序） */
    private List<RouteOpRespDTO> ops;

    /** 物料信息（可选） */
    private MaterialRespDTO mat;

}
