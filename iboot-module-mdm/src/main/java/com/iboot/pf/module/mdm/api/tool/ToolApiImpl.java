package com.iboot.pf.module.mdm.api.tool;

import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mdm.api.tool.dto.RouteOpToolRespDTO;
import com.iboot.pf.module.mdm.api.tool.dto.ToolRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.tool.RouteOpToolDO;
import com.iboot.pf.module.mdm.dal.dataobject.tool.ToolDO;
import com.iboot.pf.module.mdm.service.tool.ToolService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 工装模具刀具 API 实现类
 *
 * @author iboot
 */
@Service
@DataPermission(enable = false)
public class ToolApiImpl implements ToolApi {

    @Resource
    private ToolService toolService;

    @Override
    public List<ToolRespDTO> getToolList() {
        List<ToolDO> list = toolService.getToolList();
        return BeanUtils.toBean(list, ToolRespDTO.class);
    }

    @Override
    public List<RouteOpToolRespDTO> getRouteOpToolList(Collection<Long> routeOpIds) {
        List<RouteOpToolDO> list = toolService.getRouteOpToolList(routeOpIds);
        return BeanUtils.toBean(list, RouteOpToolRespDTO.class);
    }

}
