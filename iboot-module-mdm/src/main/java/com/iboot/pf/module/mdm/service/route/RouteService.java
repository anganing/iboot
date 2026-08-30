package com.iboot.pf.module.mdm.service.route;

import java.util.*;
import java.time.LocalDate;
import jakarta.validation.*;
import com.iboot.pf.module.mdm.controller.admin.route.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.route.RouteDO;
import com.iboot.pf.module.mdm.dal.dataobject.routeop.RouteOpDO;
import com.iboot.pf.module.mdm.dal.dataobject.routeopmat.RouteOpMatDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * 工艺路线 Service 接口
 *
 * @author iboot
 */
public interface RouteService {

    /**
     * 创建工艺路线
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createRoute(@Valid RouteSaveReqVO createReqVO);

    /**
     * 更新工艺路线
     *
     * @param updateReqVO 更新信息
     */
    void updateRoute(@Valid RouteSaveReqVO updateReqVO);

    /**
     * 删除工艺路线
     *
     * @param id 编号
     */
    void deleteRoute(Long id);

    /**
    * 批量删除工艺路线
    *
    * @param ids 编号
    */
    void deleteRouteListByIds(List<Long> ids);

    /**
     * 获得工艺路线
     *
     * @param id 编号
     * @return 工艺路线
     */
    RouteRespVO getRoute(Long id);

    RouteDetailRespVO getRouteDetail(Long id);

    /**
     * 获得工艺路线分页
     *
     * @param pageReqVO 分页查询
     * @return 工艺路线分页
     */
    PageResult<RouteRespVO> getRoutePage(RoutePageReqVO pageReqVO);

    /**
     * 复制工艺路线为新版本：连同工序、工序用料与 ESOP 指导书一并复制，
     * 新版本为停用状态，工序编号正确重指向新版本工序。
     *
     * @param copyReqVO 复制请求
     * @return 新版本路线编号
     */
    Long copyRoute(@Valid RouteCopyReqVO copyReqVO);

    // ==================== 子表（工艺路线工序） ====================

    /**
     * 获得工艺路线工序分页
     *
     * @param pageReqVO 分页查询
     * @param routeId 工艺路线编号
     * @return 工艺路线工序分页
     */
    PageResult<RouteOpDO> getRouteOpPage(PageParam pageReqVO, Long routeId);

    /**
     * 创建工艺路线工序
     *
     * @param routeOp 创建信息
     * @return 编号
     */
    Long createRouteOp(@Valid RouteOpSaveReqVO routeOp);

    /**
     * 更新工艺路线工序
     *
     * @param routeOp 更新信息
     */
    void updateRouteOp(@Valid RouteOpSaveReqVO routeOp);

    void updateRouteOpSort(@Valid RouteOpSortReqVO sortReqVO);

    /**
     * 删除工艺路线工序
     *
     * @param id 编号
     */
    void deleteRouteOp(Long id);

    /**
    * 批量删除工艺路线工序
    *
    * @param ids 编号
    */
    void deleteRouteOpListByIds(List<Long> ids);

	/**
	 * 获得工艺路线工序
	 *
	 * @param id 编号
     * @return 工艺路线工序
	 */
    RouteOpDO getRouteOp(Long id);

    // ==================== 只读门面（供 API 层委托） ====================

    /**
     * 按编号单查工艺路线（返回 DO）
     *
     * @param id 编号
     * @return 工艺路线；不存在返回 {@code null}
     */
    RouteDO getRouteDO(Long id);

    /**
     * 获得指定物料在指定日期下的生效工艺路线
     *
     * <p>规则：仅启用状态，且 effectiveDate <= date <= expiryDate；
     * 多个生效版本取 effectiveDate 最新一条；无生效版本返回 {@code null}。
     *
     * @param matId 物料编号
     * @param date 生效判定日期
     * @return 生效工艺路线；无生效版本返回 {@code null}
     */
    RouteDO getEffectiveRoute(Long matId, LocalDate date);

    /**
     * 按工艺路线编号查询其全部工序，按 seq 升序
     *
     * @param routeId 工艺路线编号
     * @return 工序列表；无工序返回空列表
     */
    List<RouteOpDO> getRouteOpList(Long routeId);

    /**
     * 按路线工序编号批量查询工序用料
     *
     * @param routeOpIds 路线工序编号集合
     * @return 工序用料列表；空集合返回空列表
     */
    List<RouteOpMatDO> getRouteOpMatList(Collection<Long> routeOpIds);

}
