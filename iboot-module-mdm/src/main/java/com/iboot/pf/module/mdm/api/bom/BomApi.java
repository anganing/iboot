package com.iboot.pf.module.mdm.api.bom;

import com.iboot.pf.module.mdm.api.bom.dto.BomDetailRespDTO;
import com.iboot.pf.module.mdm.api.bom.dto.BomLineRespDTO;
import com.iboot.pf.module.mdm.api.bom.dto.BomRespDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * 物料清单（BOM）API 接口
 *
 * <p>对 MES / APS 提供 BOM 版本与明细门面。
 *
 * @author iboot
 */
public interface BomApi {

    /**
     * 按编号单查 BOM
     *
     * @param id BOM 编号
     * @return BOM；不存在返回 {@code null}
     */
    BomRespDTO getBom(Long id);

    /**
     * 获得指定物料在指定日期下的生效 BOM 版本
     *
     * <p>仅返回启用状态，且 effectiveDate <= date <= expiryDate；
     * 多个生效版本取 effectiveDate 最新一条；无生效版本返回 {@code null}。
     *
     * @param matId 物料编号
     * @param date 生效判定日期
     * @return 生效 BOM；无生效版本返回 {@code null}
     */
    BomRespDTO getEffectiveBom(Long matId, LocalDate date);

    /**
     * 按编号查询 BOM 详情（含全部明细）
     *
     * @param id BOM 编号
     * @return BOM 详情；不存在返回 {@code null}
     */
    BomDetailRespDTO getBomDetail(Long id);

    /**
     * 按 BOM 编号查询其全部明细
     *
     * @param bomId BOM 编号
     * @return BOM 明细列表；无明细返回空列表
     */
    List<BomLineRespDTO> getBomLineList(Long bomId);

}
