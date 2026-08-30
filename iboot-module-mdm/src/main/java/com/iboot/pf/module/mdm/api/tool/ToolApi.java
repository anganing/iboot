package com.iboot.pf.module.mdm.api.tool;

import com.iboot.pf.module.mdm.api.tool.dto.RouteOpToolRespDTO;
import com.iboot.pf.module.mdm.api.tool.dto.ToolRespDTO;

import java.util.Collection;
import java.util.List;

/**
 * 工装模具刀具 API 接口
 *
 * <p>对 MES、APS 提供工装与工序工装门面。
 *
 * @author iboot
 */
public interface ToolApi {

    /**
     * 获得全部工装
     *
     * @return 工装列表；无数据返回空列表
     */
    List<ToolRespDTO> getToolList();

    /**
     * 按路线工序编号集合批量查询工序工装
     *
     * @param routeOpIds 路线工序编号集合
     * @return 工序工装列表；空集合返回空列表
     */
    List<RouteOpToolRespDTO> getRouteOpToolList(Collection<Long> routeOpIds);

}
