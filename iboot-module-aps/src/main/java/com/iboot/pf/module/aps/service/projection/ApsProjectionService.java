package com.iboot.pf.module.aps.service.projection;

import com.iboot.pf.module.aps.api.projection.dto.*;
import com.iboot.pf.module.aps.dal.dataobject.projection.*;
import java.util.List;

/** APS 数据投影服务。 */
public interface ApsProjectionService {
    void refreshMaterialProjection(RefreshProjectionReqDTO reqDTO);
    void refreshInventoryProjection(RefreshProjectionReqDTO reqDTO);
    void refreshOrderProjection(RefreshProjectionReqDTO reqDTO);
    List<ApsMaterialProjectionDO> getMaterialProjectionList(MaterialProjectionQueryReqDTO query);
    List<ApsInventoryProjectionDO> getInventoryProjectionList(InventoryProjectionQueryReqDTO query);
    List<ApsOrderProjectionDO> getOrderProjectionList(OrderProjectionQueryReqDTO query);
}
