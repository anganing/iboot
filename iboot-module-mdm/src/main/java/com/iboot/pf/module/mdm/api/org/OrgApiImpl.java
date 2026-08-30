package com.iboot.pf.module.mdm.api.org;

import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mdm.api.org.dto.InvOrgRespDTO;
import com.iboot.pf.module.mdm.api.org.dto.PlantRespDTO;
import com.iboot.pf.module.mdm.api.org.dto.WcRespDTO;
import com.iboot.pf.module.mdm.api.org.dto.WorkshopRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.invorg.InvOrgDO;
import com.iboot.pf.module.mdm.dal.dataobject.plant.PlantDO;
import com.iboot.pf.module.mdm.dal.dataobject.wc.WcDO;
import com.iboot.pf.module.mdm.dal.dataobject.workshop.WorkshopDO;
import com.iboot.pf.module.mdm.service.invorg.InvOrgService;
import com.iboot.pf.module.mdm.service.plant.PlantService;
import com.iboot.pf.module.mdm.service.wc.WcService;
import com.iboot.pf.module.mdm.service.workshop.WorkshopService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    @DataPermission(enable = false)
    public List<InvOrgRespDTO> getInvOrgList() {
        List<InvOrgDO> list = invOrgService.getInvOrgList();
        return BeanUtils.toBean(list, InvOrgRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public List<PlantRespDTO> getPlantList() {
        List<PlantDO> list = plantService.getPlantList();
        return BeanUtils.toBean(list, PlantRespDTO.class);
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
