package com.iboot.pf.module.wms.api.warehouse;

import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.wms.api.warehouse.dto.LocRespDTO;
import com.iboot.pf.module.wms.api.warehouse.dto.WhRespDTO;
import com.iboot.pf.module.wms.api.warehouse.dto.ZoneRespDTO;
import com.iboot.pf.module.wms.service.warehouse.WmsWarehouseService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 仓库结构 API 实现类。
 *
 * <p>查询方法关闭数据权限，避免内部跨模块调用被当前用户数据权限过滤。
 */
@Service
@Validated
@DataPermission(enable = false)
public class WarehouseApiImpl implements WarehouseApi {

    @Resource
    private WmsWarehouseService warehouseService;

    @Override
    public WhRespDTO getWh(Long id) {
        return BeanUtils.toBean(warehouseService.getWh(id), WhRespDTO.class);
    }

    @Override
    public List<WhRespDTO> getWhList(Collection<Long> ids) {
        return BeanUtils.toBean(warehouseService.getWhList(ids), WhRespDTO.class);
    }

    @Override
    public Map<Long, WhRespDTO> getWhMap(Collection<Long> ids) {
        return com.iboot.pf.framework.common.util.collection.CollectionUtils.convertMap(
                BeanUtils.toBean(warehouseService.getWhList(ids), WhRespDTO.class), WhRespDTO::getId);
    }

    @Override
    public ZoneRespDTO getZone(Long id) {
        return BeanUtils.toBean(warehouseService.getZone(id), ZoneRespDTO.class);
    }

    @Override
    public List<ZoneRespDTO> getZoneList(Collection<Long> ids) {
        return BeanUtils.toBean(warehouseService.getZoneList(ids), ZoneRespDTO.class);
    }

    @Override
    public Map<Long, ZoneRespDTO> getZoneMap(Collection<Long> ids) {
        return com.iboot.pf.framework.common.util.collection.CollectionUtils.convertMap(
                BeanUtils.toBean(warehouseService.getZoneList(ids), ZoneRespDTO.class), ZoneRespDTO::getId);
    }

    @Override
    public List<ZoneRespDTO> getZoneListByWh(Long whId) {
        return BeanUtils.toBean(warehouseService.getZoneListByWh(whId), ZoneRespDTO.class);
    }

    @Override
    public LocRespDTO getLoc(Long id) {
        return BeanUtils.toBean(warehouseService.getLoc(id), LocRespDTO.class);
    }

    @Override
    public List<LocRespDTO> getLocList(Collection<Long> ids) {
        return BeanUtils.toBean(warehouseService.getLocList(ids), LocRespDTO.class);
    }

    @Override
    public Map<Long, LocRespDTO> getLocMap(Collection<Long> ids) {
        return com.iboot.pf.framework.common.util.collection.CollectionUtils.convertMap(
                BeanUtils.toBean(warehouseService.getLocList(ids), LocRespDTO.class), LocRespDTO::getId);
    }

    @Override
    public List<LocRespDTO> getLocListByWh(Long whId) {
        return BeanUtils.toBean(warehouseService.getLocListByWh(whId), LocRespDTO.class);
    }

}
