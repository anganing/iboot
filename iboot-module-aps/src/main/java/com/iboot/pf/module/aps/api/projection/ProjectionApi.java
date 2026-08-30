package com.iboot.pf.module.aps.api.projection;

import com.iboot.pf.module.aps.api.projection.dto.*;
import jakarta.validation.Valid;
import java.util.List;

/** APS 数据投影 API 门面。 */
public interface ProjectionApi {
    void refreshMaterialProjection(@Valid RefreshProjectionReqDTO reqDTO);
    void refreshInventoryProjection(@Valid RefreshProjectionReqDTO reqDTO);
    void refreshOrderProjection(@Valid RefreshProjectionReqDTO reqDTO);
    List<MaterialProjectionRespDTO> getMaterialProjectionList(@Valid MaterialProjectionQueryReqDTO query);
    List<InventoryProjectionRespDTO> getInventoryProjectionList(@Valid InventoryProjectionQueryReqDTO query);
    List<OrderProjectionRespDTO> getOrderProjectionList(@Valid OrderProjectionQueryReqDTO query);
}
