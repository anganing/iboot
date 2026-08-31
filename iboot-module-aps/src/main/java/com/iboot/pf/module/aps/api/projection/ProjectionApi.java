package com.iboot.pf.module.aps.api.projection;

import com.iboot.pf.module.aps.api.projection.dto.InventoryProjectionQueryReqDTO;
import com.iboot.pf.module.aps.api.projection.dto.InventoryProjectionRespDTO;
import com.iboot.pf.module.aps.api.projection.dto.MaterialProjectionQueryReqDTO;
import com.iboot.pf.module.aps.api.projection.dto.MaterialProjectionRespDTO;
import com.iboot.pf.module.aps.api.projection.dto.OrderProjectionQueryReqDTO;
import com.iboot.pf.module.aps.api.projection.dto.OrderProjectionRespDTO;
import com.iboot.pf.module.aps.api.projection.dto.RefreshProjectionReqDTO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * APS 数据投影 API 门面。
 *
 * <p>供 MES 与集成侧在下发排程前刷新输入快照，或读取某个场景冻结的输入。
 */
public interface ProjectionApi {

    /** 刷新物料投影。 */
    void refreshMaterialProjection(@Valid RefreshProjectionReqDTO reqDTO);

    /** 刷新库存投影。 */
    void refreshInventoryProjection(@Valid RefreshProjectionReqDTO reqDTO);

    /** 刷新工单投影。 */
    void refreshOrderProjection(@Valid RefreshProjectionReqDTO reqDTO);

    /** 刷新资源与日历投影。 */
    void refreshResourceProjection(@Valid RefreshProjectionReqDTO reqDTO);

    /** 查询物料投影。 */
    List<MaterialProjectionRespDTO> getMaterialProjectionList(@Valid MaterialProjectionQueryReqDTO query);

    /** 查询库存投影。 */
    List<InventoryProjectionRespDTO> getInventoryProjectionList(@Valid InventoryProjectionQueryReqDTO query);

    /** 查询工单投影。 */
    List<OrderProjectionRespDTO> getOrderProjectionList(@Valid OrderProjectionQueryReqDTO query);

}
