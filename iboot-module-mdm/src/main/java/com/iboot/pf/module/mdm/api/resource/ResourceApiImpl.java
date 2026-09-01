package com.iboot.pf.module.mdm.api.resource;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.common.util.collection.CollectionUtils;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mdm.api.resource.dto.ResChangeoverRespDTO;
import com.iboot.pf.module.mdm.api.resource.dto.ResourceCalendarRespDTO;
import com.iboot.pf.module.mdm.api.resource.dto.ResourceCapabilityRespDTO;
import com.iboot.pf.module.mdm.api.resource.dto.ResourceRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.changeover.ResChangeoverDO;
import com.iboot.pf.module.mdm.dal.dataobject.res.ResDO;
import com.iboot.pf.module.mdm.dal.dataobject.rescalendar.ResCalendarDO;
import com.iboot.pf.module.mdm.dal.dataobject.rescapability.ResCapabilityDO;
import com.iboot.pf.module.mdm.service.changeover.ResChangeoverService;
import com.iboot.pf.module.mdm.service.res.ResService;
import com.iboot.pf.module.mdm.service.rescalendar.ResCalendarService;
import com.iboot.pf.module.mdm.service.rescapability.ResCapabilityService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 生产资源 API 实现类
 *
 * @author iboot
 */
@Service
public class ResourceApiImpl implements ResourceApi {

    @Resource
    private ResService resService;
    @Resource
    private ResCalendarService resCalendarService;
    @Resource
    private ResCapabilityService resCapabilityService;
    @Resource
    private ResChangeoverService resChangeoverService;

    @Override
    @DataPermission(enable = false)
    public ResourceRespDTO getResource(Long id) {
        List<ResourceRespDTO> list = getResourceList(Collections.singleton(id));
        return CollUtil.getFirst(list);
    }

    @Override
    @DataPermission(enable = false)
    public List<ResourceRespDTO> getResourceList(Collection<Long> ids) {
        List<ResDO> res = resService.getResList(ids);
        return BeanUtils.toBean(res, ResourceRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public Map<Long, ResourceRespDTO> getResourceMap(Collection<Long> ids) {
        List<ResourceRespDTO> list = getResourceList(ids);
        return CollectionUtils.convertMap(list, ResourceRespDTO::getId);
    }

    @Override
    @DataPermission(enable = false)
    public List<ResourceRespDTO> getResourceListByPlantId(Long plantId) {
        // 停用资源不参与排程，直接在门面这层过滤，调用方不必再判断状态
        List<ResDO> res = resService.getResListByPlantId(plantId).stream()
                .filter(item -> CommonStatusEnum.isEnable(item.getStatus()))
                .toList();
        return BeanUtils.toBean(res, ResourceRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public List<ResourceCalendarRespDTO> getResourceCalendarList(Collection<Long> resIds,
                                                                 LocalDate startDate,
                                                                 LocalDate endDate) {
        List<ResCalendarDO> calendars = resCalendarService.getResCalendarList(resIds, startDate, endDate);
        return BeanUtils.toBean(calendars, ResourceCalendarRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public List<ResourceCapabilityRespDTO> getResourceCapabilityList(Collection<Long> opIds) {
        List<ResCapabilityDO> capabilities = resCapabilityService.getResCapabilityList(opIds);
        return BeanUtils.toBean(capabilities, ResourceCapabilityRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public List<ResourceCapabilityRespDTO> getResourceCapabilityListByResIds(Collection<Long> resIds) {
        List<ResCapabilityDO> capabilities = resCapabilityService.getResCapabilityListByResIds(resIds);
        return BeanUtils.toBean(capabilities, ResourceCapabilityRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public List<ResChangeoverRespDTO> getChangeoverList(Collection<Long> resIds, Collection<String> resGroups) {
        List<ResChangeoverDO> changeovers = resChangeoverService.getChangeoverList(resIds, resGroups);
        return BeanUtils.toBean(changeovers, ResChangeoverRespDTO.class);
    }

}
