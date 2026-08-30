package com.iboot.pf.module.mdm.service.bom;

import java.util.*;
import java.time.LocalDate;
import jakarta.validation.*;
import com.iboot.pf.module.mdm.controller.admin.bom.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.bom.BomDO;
import com.iboot.pf.module.mdm.dal.dataobject.bomline.BomLineDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * 物料清单 Service 接口
 *
 * @author iboot
 */
public interface BomService {

    /**
     * 创建物料清单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createBom(@Valid BomSaveReqVO createReqVO);

    /**
     * 更新物料清单
     *
     * @param updateReqVO 更新信息
     */
    void updateBom(@Valid BomSaveReqVO updateReqVO);

    /**
     * 删除物料清单
     *
     * @param id 编号
     */
    void deleteBom(Long id);

    /**
    * 批量删除物料清单
    *
    * @param ids 编号
    */
    void deleteBomListByIds(List<Long> ids);

    /**
     * 获得物料清单
     *
     * @param id 编号
     * @return 物料清单
     */
    BomRespVO getBom(Long id);

    /**
     * 获得物料清单分页
     *
     * @param pageReqVO 分页查询
     * @return 物料清单分页
     */
    PageResult<BomRespVO> getBomPage(BomPageReqVO pageReqVO);

    // ==================== 子表（物料清单明细） ====================

    /**
     * 获得物料清单明细分页
     *
     * @param pageReqVO 分页查询
     * @param bomId BOM编号
     * @return 物料清单明细分页
     */
    PageResult<BomLineRespVO> getBomLinePage(PageParam pageReqVO, Long bomId);

    /**
     * 创建物料清单明细
     *
     * @param bomLine 创建信息
     * @return 编号
     */
    Long createBomLine(@Valid BomLineDO bomLine);

    /**
     * 更新物料清单明细
     *
     * @param bomLine 更新信息
     */
    void updateBomLine(@Valid BomLineDO bomLine);

    /**
     * 删除物料清单明细
     *
     * @param id 编号
     */
    void deleteBomLine(Long id);

    /**
    * 批量删除物料清单明细
    *
    * @param ids 编号
    */
    void deleteBomLineListByIds(List<Long> ids);

	/**
	 * 获得物料清单明细
	 *
	 * @param id 编号
     * @return 物料清单明细
	 */
    BomLineRespVO getBomLine(Long id);

    // ==================== 只读门面（供 API 层委托） ====================

    /**
     * 按编号单查 BOM（返回 DO）
     *
     * @param id 编号
     * @return BOM；不存在返回 {@code null}
     */
    BomDO getBomDO(Long id);

    /**
     * 获得指定物料在指定日期下的生效 BOM 版本
     *
     * <p>规则：仅启用状态，且 effectiveDate <= date <= expiryDate；
     * 多个生效版本取 effectiveDate 最新一条；无生效版本返回 {@code null}。
     *
     * @param matId 物料编号
     * @param date 生效判定日期
     * @return 生效 BOM；无生效版本返回 {@code null}
     */
    BomDO getEffectiveBom(Long matId, LocalDate date);

    /**
     * 按 BOM 编号查询其全部明细
     *
     * @param bomId BOM 编号
     * @return BOM 明细列表；无明细返回空列表
     */
    List<BomLineDO> getBomLineList(Long bomId);

}