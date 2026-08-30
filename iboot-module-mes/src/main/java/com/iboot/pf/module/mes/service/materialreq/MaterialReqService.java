package com.iboot.pf.module.mes.service.materialreq;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.materialreq.MaterialReqDO;
import com.iboot.pf.module.mes.dal.dataobject.materialreqline.MaterialReqLineDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * MES-领退补料申请 Service 接口
 *
 * @author iboot
 */
public interface MaterialReqService {

    /**
     * 创建MES-领退补料申请
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMaterialReq(@Valid MaterialReqSaveReqVO createReqVO);

    /**
     * 导入MES-领退补料申请
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    MaterialReqImportRespVO importMaterialReqList(List<MaterialReqImportExcelVO> importList);

    /**
     * 更新MES-领退补料申请
     *
     * @param updateReqVO 更新信息
     */
    void updateMaterialReq(@Valid MaterialReqSaveReqVO updateReqVO);

    /**
     * 删除MES-领退补料申请
     *
     * @param id 编号
     */
    void deleteMaterialReq(Long id);

    /**
    * 批量删除MES-领退补料申请
    *
    * @param ids 编号
    */
    void deleteMaterialReqListByIds(List<Long> ids);

    /**
     * 获得MES-领退补料申请
     *
     * @param id 编号
     * @return MES-领退补料申请
     */
    MaterialReqRespVO getMaterialReq(Long id);

    /**
     * 获得MES-领退补料申请分页
     *
     * @param pageReqVO 分页查询
     * @return MES-领退补料申请分页
     */
    PageResult<MaterialReqRespVO> getMaterialReqPage(MaterialReqPageReqVO pageReqVO);

    // ==================== 子表（MES-领退补料申请明细） ====================

    /**
     * 获得MES-领退补料申请明细分页
     *
     * @param pageReqVO 分页查询
     * @param reqId 领退补料申请编号
     * @return MES-领退补料申请明细分页
     */
    PageResult<MaterialReqLineDO> getMaterialReqLinePage(PageParam pageReqVO, Long reqId);

    /**
     * 创建MES-领退补料申请明细
     *
     * @param materialReqLine 创建信息
     * @return 编号
     */
    Long createMaterialReqLine(@Valid MaterialReqLineDO materialReqLine);

    /**
     * 更新MES-领退补料申请明细
     *
     * @param materialReqLine 更新信息
     */
    void updateMaterialReqLine(@Valid MaterialReqLineDO materialReqLine);

    /**
     * 删除MES-领退补料申请明细
     *
     * @param id 编号
     */
    void deleteMaterialReqLine(Long id);

    /**
    * 批量删除MES-领退补料申请明细
    *
    * @param ids 编号
    */
    void deleteMaterialReqLineListByIds(List<Long> ids);

	/**
	 * 获得MES-领退补料申请明细
	 *
	 * @param id 编号
     * @return MES-领退补料申请明细
	 */
    MaterialReqLineDO getMaterialReqLine(Long id);

}