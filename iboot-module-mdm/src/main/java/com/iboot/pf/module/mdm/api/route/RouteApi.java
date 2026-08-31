package com.iboot.pf.module.mdm.api.route;

import com.iboot.pf.module.mdm.api.route.dto.RouteDetailRespDTO;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpEsopRespDTO;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpMatRespDTO;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpRespDTO;
import com.iboot.pf.module.mdm.api.route.dto.RouteRespDTO;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 工艺路线 API 接口
 *
 * <p>对 MES / APS 提供工艺路线、工序、用料、ESOP 门面。
 *
 * @author iboot
 */
public interface RouteApi {

    /**
     * 按编号单查工艺路线
     *
     * @param id 工艺路线编号
     * @return 工艺路线；不存在返回 {@code null}
     */
    RouteRespDTO getRoute(Long id);

    /**
     * 获得指定物料在指定日期下的生效工艺路线
     *
     * <p>仅返回启用状态，且 effectiveDate <= date <= expiryDate；
     * 多个生效版本取 effectiveDate 最新一条；无生效版本返回 {@code null}。
     *
     * @param matId 物料编号
     * @param date 生效判定日期
     * @return 生效工艺路线；无生效版本返回 {@code null}
     */
    RouteRespDTO getEffectiveRoute(Long matId, LocalDate date);

    /**
     * 按编号查询工艺路线详情（含按 seq 升序的工序列表）
     *
     * @param id 工艺路线编号
     * @return 工艺路线详情；不存在返回 {@code null}
     */
    RouteDetailRespDTO getRouteDetail(Long id);

    /**
     * 按工艺路线编号查询其全部工序，按 seq 升序
     *
     * @param routeId 工艺路线编号
     * @return 工序列表；无工序返回空列表
     */
    List<RouteOpRespDTO> getRouteOpList(Long routeId);

    /**
     * 按路线工序编号集合批量查询路线工序 Map，供批量补全
     *
     * <p>MES 工序任务锁定 routeOpId，创建任务时要从这里取工序顺序、并行组、关键工序标记。
     *
     * @param routeOpIds 路线工序编号集合
     * @return 路线工序编号 -> 路线工序；缺失编号不产生键
     */
    Map<Long, RouteOpRespDTO> getRouteOpMap(Collection<Long> routeOpIds);

    /**
     * 按路线工序编号批量查询工序用料
     *
     * @param routeOpIds 路线工序编号集合
     * @return 工序用料列表；空集合返回空列表
     */
    List<RouteOpMatRespDTO> getRouteOpMatList(Collection<Long> routeOpIds);

    /**
     * 按路线工序编号批量查询 ESOP 作业步骤（含附件，附件 fileUrl 为预签名地址）
     *
     * @param routeOpIds 路线工序编号集合
     * @return 作业步骤列表；空集合返回空列表
     */
    List<RouteOpEsopRespDTO> getRouteOpEsopList(Collection<Long> routeOpIds);

}
