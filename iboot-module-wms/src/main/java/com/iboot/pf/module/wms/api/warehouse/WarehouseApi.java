package com.iboot.pf.module.wms.api.warehouse;

import com.iboot.pf.module.wms.api.warehouse.dto.LocRespDTO;
import com.iboot.pf.module.wms.api.warehouse.dto.WhRespDTO;
import com.iboot.pf.module.wms.api.warehouse.dto.ZoneRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 仓库结构 API 门面：仓库、库区、库位的只读查询。
 *
 * <p>向 MES / APS / UI 提供仓库结构主数据，用于库存定位、作业目标库位选择。
 */
public interface WarehouseApi {

    WhRespDTO getWh(Long id);

    List<WhRespDTO> getWhList(Collection<Long> ids);

    Map<Long, WhRespDTO> getWhMap(Collection<Long> ids);

    ZoneRespDTO getZone(Long id);

    List<ZoneRespDTO> getZoneList(Collection<Long> ids);

    Map<Long, ZoneRespDTO> getZoneMap(Collection<Long> ids);

    /** 按仓库查库区列表。 */
    List<ZoneRespDTO> getZoneListByWh(Long whId);

    LocRespDTO getLoc(Long id);

    List<LocRespDTO> getLocList(Collection<Long> ids);

    Map<Long, LocRespDTO> getLocMap(Collection<Long> ids);

    /** 按仓库查库位列表。 */
    List<LocRespDTO> getLocListByWh(Long whId);

}
