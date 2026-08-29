package com.iboot.pf.module.mdm.service.tool;

import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mdm.controller.admin.tool.vo.RouteOpToolRespVO;
import com.iboot.pf.module.mdm.controller.admin.tool.vo.RouteOpToolSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.tool.vo.ToolPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.tool.vo.ToolRespVO;
import com.iboot.pf.module.mdm.controller.admin.tool.vo.ToolSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.tool.RouteOpToolDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 工装模具刀具 Service 接口
 *
 * @author iboot
 */
public interface ToolService {

    // ==================== 工装 ====================

    /**
     * 创建工装
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTool(@Valid ToolSaveReqVO createReqVO);

    /**
     * 更新工装
     *
     * @param updateReqVO 更新信息
     */
    void updateTool(@Valid ToolSaveReqVO updateReqVO);

    /**
     * 删除工装（被工序引用时不允许删除）
     *
     * @param id 编号
     */
    void deleteTool(Long id);

    /**
     * 批量删除工装
     *
     * @param ids 编号列表
     */
    void deleteToolListByIds(List<Long> ids);

    /**
     * 获得工装
     *
     * @param id 编号
     * @return 工装
     */
    ToolRespVO getTool(Long id);

    /**
     * 获得工装分页
     *
     * @param pageReqVO 分页查询
     * @return 工装分页
     */
    PageResult<ToolRespVO> getToolPage(ToolPageReqVO pageReqVO);

    // ==================== 工序工装 ====================

    /**
     * 创建工序工装
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createRouteOpTool(@Valid RouteOpToolSaveReqVO createReqVO);

    /**
     * 删除工序工装
     *
     * @param id 编号
     */
    void deleteRouteOpTool(Long id);

    /**
     * 获得工序工装分页
     *
     * @param pageParam 分页参数
     * @param routeOpId 路线工序编号
     * @return 工序工装分页
     */
    PageResult<RouteOpToolRespVO> getRouteOpToolPage(PageParam pageParam, Long routeOpId);

    /**
     * 获得工序的工装列表
     *
     * @param routeOpId 路线工序编号
     * @return 工装列表
     */
    List<RouteOpToolDO> getRouteOpToolListByRouteOpId(Long routeOpId);

}
