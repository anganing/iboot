package com.iboot.pf.module.mdm.service.esop;

import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mdm.controller.admin.esop.vo.RouteOpEsopPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.esop.vo.RouteOpEsopRespVO;
import com.iboot.pf.module.mdm.controller.admin.esop.vo.RouteOpEsopSaveReqVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 工序 ESOP 作业步骤 Service 接口
 *
 * @author iboot
 */
public interface RouteOpEsopService {

    /**
     * 创建 ESOP 作业步骤（工艺路线启用后禁止写入）
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createRouteOpEsop(@Valid RouteOpEsopSaveReqVO createReqVO);

    /**
     * 更新 ESOP 作业步骤（工艺路线启用后禁止写入）
     *
     * @param updateReqVO 更新信息
     */
    void updateRouteOpEsop(@Valid RouteOpEsopSaveReqVO updateReqVO);

    /**
     * 删除 ESOP 作业步骤（工艺路线启用后禁止删除，附件级联删除）
     *
     * @param id 编号
     */
    void deleteRouteOpEsop(Long id);

    /**
     * 批量删除 ESOP 作业步骤
     *
     * @param ids 编号列表
     */
    void deleteRouteOpEsopListByIds(List<Long> ids);

    /**
     * 获得 ESOP 作业步骤（含附件列表）
     *
     * @param id 编号
     * @return ESOP 作业步骤
     */
    RouteOpEsopRespVO getRouteOpEsop(Long id);

    /**
     * 获得 ESOP 作业步骤分页
     *
     * @param pageParam 分页参数
     * @param pageReqVO 查询条件
     * @return ESOP 作业步骤分页
     */
    PageResult<RouteOpEsopRespVO> getRouteOpEsopPage(PageParam pageParam, RouteOpEsopPageReqVO pageReqVO);

    /**
     * 按路线工序编号批量查询作业步骤（含附件），MES 工序任务持有 route_op_id 直接反查。
     *
     * @param routeOpId 路线工序编号
     * @return 作业步骤列表
     */
    List<RouteOpEsopRespVO> getRouteOpEsopListByRouteOpId(Long routeOpId);

}
