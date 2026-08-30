package com.iboot.pf.module.wms.service.warehouse;

import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsLocDO;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsWhDO;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsZoneDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/** 仓库结构 Service：仓库、库区、库位。 */
public interface WmsWarehouseService {

    WmsWhDO getWh(Long id);

    List<WmsWhDO> getWhList(Collection<Long> ids);

    Map<Long, WmsWhDO> getWhMap(Collection<Long> ids);

    /** 校验仓库存在且启用，否则抛业务异常。 */
    void validateWh(Long id);

    WmsZoneDO getZone(Long id);

    List<WmsZoneDO> getZoneList(Collection<Long> ids);

    Map<Long, WmsZoneDO> getZoneMap(Collection<Long> ids);

    List<WmsZoneDO> getZoneListByWh(Long whId);

    WmsLocDO getLoc(Long id);

    List<WmsLocDO> getLocList(Collection<Long> ids);

    Map<Long, WmsLocDO> getLocMap(Collection<Long> ids);

    List<WmsLocDO> getLocListByWh(Long whId);

    /** 按库区查库位，盘点按库区圈选库存时使用。 */
    List<WmsLocDO> getLocListByZone(Long zoneId);

    /** 校验库位存在且未停用，否则抛业务异常。 */
    void validateLoc(Long id);

}
