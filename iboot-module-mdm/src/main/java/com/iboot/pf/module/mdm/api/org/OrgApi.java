package com.iboot.pf.module.mdm.api.org;

import com.iboot.pf.module.mdm.api.org.dto.InvOrgRespDTO;
import com.iboot.pf.module.mdm.api.org.dto.PlantRespDTO;
import com.iboot.pf.module.mdm.api.org.dto.WcRespDTO;
import com.iboot.pf.module.mdm.api.org.dto.WorkshopRespDTO;

import java.util.List;

/**
 * 组织与工作中心 API 接口
 *
 * <p>对 WMS / MES / APS 提供库存组织、工厂、车间、工作中心门面。
 *
 * @author iboot
 */
public interface OrgApi {

    /**
     * 获得全部库存组织（含停用）
     *
     * @return 库存组织列表
     */
    List<InvOrgRespDTO> getInvOrgList();

    /**
     * 获得全部工厂（含停用）
     *
     * @return 工厂列表
     */
    List<PlantRespDTO> getPlantList();

    /**
     * 获得全部车间（含停用）
     *
     * @return 车间列表
     */
    List<WorkshopRespDTO> getWorkshopList();

    /**
     * 获得全部工作中心（含停用）
     *
     * @return 工作中心列表
     */
    List<WcRespDTO> getWcList();

}
