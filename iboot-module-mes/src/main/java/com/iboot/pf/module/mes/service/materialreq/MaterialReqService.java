package com.iboot.pf.module.mes.service.materialreq;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqLinePageReqVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqLineRespVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqLineSaveReqVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqPageReqVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqRespVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqSaveReqVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES-领退补料申请 Service 接口
 *
 * @author iboot
 */
public interface MaterialReqService {

    /**
     * 创建领退补料申请
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMaterialReq(@Valid MaterialReqSaveReqVO createReqVO);

    /**
     * 更新领退补料申请
     *
     * @param updateReqVO 更新信息
     */
    void updateMaterialReq(@Valid MaterialReqSaveReqVO updateReqVO);

    /**
     * 删除领退补料申请，同时级联删除明细
     *
     * @param id 编号
     */
    void deleteMaterialReq(Long id);

    /**
     * 批量删除领退补料申请，同时级联删除明细
     *
     * @param ids 编号
     */
    void deleteMaterialReqListByIds(List<Long> ids);

    /**
     * 获得领退补料申请
     *
     * @param id 编号
     * @return 领退补料申请
     */
    MaterialReqRespVO getMaterialReq(Long id);

    /**
     * 获得领退补料申请分页
     *
     * @param pageReqVO 分页查询
     * @return 领退补料申请分页
     */
    PageResult<MaterialReqRespVO> getMaterialReqPage(MaterialReqPageReqVO pageReqVO);

    // ==================== 子表（领退补料申请明细） ====================

    /**
     * 获得申请明细分页
     *
     * @param pageReqVO 分页查询，父键必填
     * @return 申请明细分页
     */
    PageResult<MaterialReqLineRespVO> getMaterialReqLinePage(@Valid MaterialReqLinePageReqVO pageReqVO);

    /**
     * 创建申请明细
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMaterialReqLine(@Valid MaterialReqLineSaveReqVO createReqVO);

    /**
     * 更新申请明细
     *
     * @param updateReqVO 更新信息
     */
    void updateMaterialReqLine(@Valid MaterialReqLineSaveReqVO updateReqVO);

    /**
     * 删除申请明细
     *
     * @param id 编号
     */
    void deleteMaterialReqLine(Long id);

    /**
     * 批量删除申请明细
     *
     * @param ids 编号
     */
    void deleteMaterialReqLineListByIds(List<Long> ids);

    /**
     * 获得申请明细
     *
     * @param id 编号
     * @return 申请明细
     */
    MaterialReqLineRespVO getMaterialReqLine(Long id);

}
