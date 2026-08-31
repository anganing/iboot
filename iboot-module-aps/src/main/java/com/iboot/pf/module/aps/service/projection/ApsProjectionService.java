package com.iboot.pf.module.aps.service.projection;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.aps.api.projection.dto.InventoryProjectionQueryReqDTO;
import com.iboot.pf.module.aps.api.projection.dto.MaterialProjectionQueryReqDTO;
import com.iboot.pf.module.aps.api.projection.dto.OrderProjectionQueryReqDTO;
import com.iboot.pf.module.aps.api.projection.dto.RefreshProjectionReqDTO;
import com.iboot.pf.module.aps.controller.admin.projectioninv.vo.ProjectionInvPageReqVO;
import com.iboot.pf.module.aps.controller.admin.projectioninv.vo.ProjectionInvRespVO;
import com.iboot.pf.module.aps.controller.admin.projectionmat.vo.ProjectionMatPageReqVO;
import com.iboot.pf.module.aps.controller.admin.projectionmat.vo.ProjectionMatRespVO;
import com.iboot.pf.module.aps.controller.admin.projectionorder.vo.ProjectionOrderPageReqVO;
import com.iboot.pf.module.aps.controller.admin.projectionorder.vo.ProjectionOrderRespVO;
import com.iboot.pf.module.aps.controller.admin.projectionresource.vo.ProjectionResourcePageReqVO;
import com.iboot.pf.module.aps.controller.admin.projectionresource.vo.ProjectionResourceRespVO;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsInventoryProjectionDO;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsMaterialProjectionDO;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsOrderProjectionDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * APS 数据投影服务。
 *
 * <p>四张投影表（物料、库存、工单、资源）都是场景冻结的只读输入：写入路径只有“刷新投影”，
 * 每次按场景整体重建。管理后台只查看与导出，不提供新增、修改、删除、导入。
 *
 * @author iboot
 */
public interface ApsProjectionService {

    /**
     * 刷新物料投影
     *
     * @param reqDTO 命令，需带上物料范围
     */
    void refreshMaterialProjection(@Valid RefreshProjectionReqDTO reqDTO);

    /**
     * 刷新库存投影
     *
     * @param reqDTO 命令
     */
    void refreshInventoryProjection(@Valid RefreshProjectionReqDTO reqDTO);

    /**
     * 刷新工单投影
     *
     * @param reqDTO 命令
     */
    void refreshOrderProjection(@Valid RefreshProjectionReqDTO reqDTO);

    /**
     * 刷新资源与日历投影
     *
     * @param reqDTO 命令
     */
    void refreshResourceProjection(@Valid RefreshProjectionReqDTO reqDTO);

    /**
     * 获得物料投影分页
     *
     * @param pageReqVO 分页查询
     * @return 物料投影分页
     */
    PageResult<ProjectionMatRespVO> getMaterialProjectionPage(ProjectionMatPageReqVO pageReqVO);

    /**
     * 获得物料投影
     *
     * @param id 编号
     * @return 物料投影
     */
    ProjectionMatRespVO getMaterialProjection(Long id);

    /**
     * 获得库存投影分页
     *
     * @param pageReqVO 分页查询
     * @return 库存投影分页
     */
    PageResult<ProjectionInvRespVO> getInventoryProjectionPage(ProjectionInvPageReqVO pageReqVO);

    /**
     * 获得库存投影
     *
     * @param id 编号
     * @return 库存投影
     */
    ProjectionInvRespVO getInventoryProjection(Long id);

    /**
     * 获得工单投影分页
     *
     * @param pageReqVO 分页查询
     * @return 工单投影分页
     */
    PageResult<ProjectionOrderRespVO> getOrderProjectionPage(ProjectionOrderPageReqVO pageReqVO);

    /**
     * 获得工单投影
     *
     * @param id 编号
     * @return 工单投影
     */
    ProjectionOrderRespVO getOrderProjection(Long id);

    /**
     * 获得资源投影分页
     *
     * @param pageReqVO 分页查询
     * @return 资源投影分页
     */
    PageResult<ProjectionResourceRespVO> getResourceProjectionPage(ProjectionResourcePageReqVO pageReqVO);

    /**
     * 获得资源投影
     *
     * @param id 编号
     * @return 资源投影
     */
    ProjectionResourceRespVO getResourceProjection(Long id);

    /**
     * 按条件获得物料投影列表，供求解输入与跨模块门面使用
     *
     * @param query 查询条件
     * @return 物料投影列表
     */
    List<ApsMaterialProjectionDO> getMaterialProjectionList(MaterialProjectionQueryReqDTO query);

    /**
     * 按条件获得库存投影列表
     *
     * @param query 查询条件
     * @return 库存投影列表
     */
    List<ApsInventoryProjectionDO> getInventoryProjectionList(InventoryProjectionQueryReqDTO query);

    /**
     * 按条件获得工单投影列表
     *
     * @param query 查询条件
     * @return 工单投影列表
     */
    List<ApsOrderProjectionDO> getOrderProjectionList(OrderProjectionQueryReqDTO query);

}
