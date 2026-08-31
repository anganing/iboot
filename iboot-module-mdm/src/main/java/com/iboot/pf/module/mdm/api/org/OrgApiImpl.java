package com.iboot.pf.module.mdm.api.org;

import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.common.util.collection.CollectionUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mdm.api.org.dto.InvOrgRespDTO;
import com.iboot.pf.module.mdm.api.org.dto.PlantRespDTO;
import com.iboot.pf.module.mdm.api.org.dto.WcRespDTO;
import com.iboot.pf.module.mdm.api.org.dto.WorkshopRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.invorg.InvOrgDO;
import com.iboot.pf.module.mdm.dal.dataobject.plant.PlantDO;
import com.iboot.pf.module.mdm.dal.dataobject.wc.WcDO;
import com.iboot.pf.module.mdm.dal.dataobject.workshop.WorkshopDO;
import com.iboot.pf.module.mdm.dal.mysql.invorg.InvOrgMapper;
import com.iboot.pf.module.mdm.dal.mysql.plant.PlantMapper;
import com.iboot.pf.module.mdm.service.invorg.InvOrgService;
import com.iboot.pf.module.mdm.service.plant.PlantService;
import com.iboot.pf.module.mdm.service.wc.WcService;
import com.iboot.pf.module.mdm.service.workshop.WorkshopService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * 组织与工作中心 API 实现类
 *
 * @author iboot
 */
@Service
public class OrgApiImpl implements OrgApi {

    @Resource
    private InvOrgService invOrgService;
    @Resource
    private PlantService plantService;
    @Resource
    private WorkshopService workshopService;
    @Resource
    private WcService wcService;
    @Resource
    private InvOrgMapper invOrgMapper;
    @Resource
    private PlantMapper plantMapper;

    @Override
    @DataPermission(enable = false)
    public List<InvOrgRespDTO> getInvOrgList() {
        List<InvOrgDO> list = invOrgService.getInvOrgList();
        return BeanUtils.toBean(list, InvOrgRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public Map<Long, InvOrgRespDTO> getInvOrgMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        // 一次 IN 查询，避免调用方 N+1
        List<InvOrgDO> list = invOrgMapper.selectByIds(ids);
        return CollectionUtils.convertMap(BeanUtils.toBean(list, InvOrgRespDTO.class), InvOrgRespDTO::getId);
    }

    @Override
    @DataPermission(enable = false)
    public List<PlantRespDTO> getPlantList() {
        List<PlantDO> list = plantService.getPlantList();
        return BeanUtils.toBean(list, PlantRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public Map<Long, PlantRespDTO> getPlantMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<PlantDO> list = plantMapper.selectByIds(ids);
        return CollectionUtils.convertMap(BeanUtils.toBean(list, PlantRespDTO.class), PlantRespDTO::getId);
    }

    @Override
    @DataPermission(enable = false)
    public List<WorkshopRespDTO> getWorkshopList() {
        List<WorkshopDO> list = workshopService.getWorkshopList();
        return BeanUtils.toBean(list, WorkshopRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public List<WcRespDTO> getWcList() {
        List<WcDO> list = wcService.getWcList();
        return BeanUtils.toBean(list, WcRespDTO.class);
    }

}
