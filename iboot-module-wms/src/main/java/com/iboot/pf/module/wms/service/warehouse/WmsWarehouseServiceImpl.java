package com.iboot.pf.module.wms.service.warehouse;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.common.util.collection.CollectionUtils;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsLocDO;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsWhDO;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsZoneDO;
import com.iboot.pf.module.wms.dal.mysql.warehouse.WmsLocMapper;
import com.iboot.pf.module.wms.dal.mysql.warehouse.WmsWhMapper;
import com.iboot.pf.module.wms.dal.mysql.warehouse.WmsZoneMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.LOC_DISABLED;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.LOC_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.WH_DISABLED;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.WH_NOT_EXISTS;

/** 仓库结构 Service 实现。 */
@Service
public class WmsWarehouseServiceImpl implements WmsWarehouseService {

    /** 库位停用状态。 */
    private static final String LOC_STATUS_DISABLED = "DISABLED";

    @Resource
    private WmsWhMapper wmsWhMapper;
    @Resource
    private WmsZoneMapper wmsZoneMapper;
    @Resource
    private WmsLocMapper wmsLocMapper;

    @Override
    public WmsWhDO getWh(Long id) {
        return wmsWhMapper.selectById(id);
    }

    @Override
    public List<WmsWhDO> getWhList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return wmsWhMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, WmsWhDO> getWhMap(Collection<Long> ids) {
        return CollectionUtils.convertMap(getWhList(ids), WmsWhDO::getId);
    }

    @Override
    public void validateWh(Long id) {
        WmsWhDO wh = wmsWhMapper.selectById(id);
        if (wh == null) {
            throw exception(WH_NOT_EXISTS);
        }
        if (CommonStatusEnum.isDisable(wh.getStatus())) {
            throw exception(WH_DISABLED);
        }
    }

    @Override
    public WmsZoneDO getZone(Long id) {
        return wmsZoneMapper.selectById(id);
    }

    @Override
    public List<WmsZoneDO> getZoneList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return wmsZoneMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, WmsZoneDO> getZoneMap(Collection<Long> ids) {
        return CollectionUtils.convertMap(getZoneList(ids), WmsZoneDO::getId);
    }

    @Override
    public List<WmsZoneDO> getZoneListByWh(Long whId) {
        return wmsZoneMapper.selectList(WmsZoneDO::getWhId, whId);
    }

    @Override
    public WmsLocDO getLoc(Long id) {
        return wmsLocMapper.selectById(id);
    }

    @Override
    public List<WmsLocDO> getLocList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return wmsLocMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, WmsLocDO> getLocMap(Collection<Long> ids) {
        return CollectionUtils.convertMap(getLocList(ids), WmsLocDO::getId);
    }

    @Override
    public List<WmsLocDO> getLocListByWh(Long whId) {
        return wmsLocMapper.selectList(WmsLocDO::getWhId, whId);
    }

    @Override
    public List<WmsLocDO> getLocListByZone(Long zoneId) {
        return wmsLocMapper.selectList(WmsLocDO::getZoneId, zoneId);
    }

    @Override
    public void validateLoc(Long id) {
        WmsLocDO loc = wmsLocMapper.selectById(id);
        if (loc == null) {
            throw exception(LOC_NOT_EXISTS);
        }
        if (LOC_STATUS_DISABLED.equals(loc.getStatus())) {
            throw exception(LOC_DISABLED);
        }
    }

}
