package com.iboot.pf.module.aps.service.projection.source;

import com.iboot.pf.module.aps.dal.dataobject.projection.ApsOrderProjectionDO;
import java.util.List;

/** MES 工单投影来源端口，待 MES API 契约落地后由适配器实现。 */
public interface OrderProjectionSource {
    List<ApsOrderProjectionDO> loadOrderProjection(Long scenarioId, Long plantId);
}
