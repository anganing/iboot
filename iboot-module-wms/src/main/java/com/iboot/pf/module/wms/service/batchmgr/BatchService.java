package com.iboot.pf.module.wms.service.batchmgr;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.wms.controller.admin.batchmgr.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.batch.WmsBatchDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * WMS-批次主档 Service 接口
 *
 * @author iboot
 */
public interface BatchService {

    /**
     * 创建WMS-批次主档
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createBatch(@Valid BatchSaveReqVO createReqVO);

    /**
     * 导入WMS-批次主档
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    BatchImportRespVO importBatchList(List<BatchImportExcelVO> importList);

    /**
     * 更新WMS-批次主档
     *
     * @param updateReqVO 更新信息
     */
    void updateBatch(@Valid BatchSaveReqVO updateReqVO);

    /**
     * 删除WMS-批次主档
     *
     * @param id 编号
     */
    void deleteBatch(Long id);

    /**
    * 批量删除WMS-批次主档
    *
    * @param ids 编号
    */
    void deleteBatchListByIds(List<Long> ids);

    /**
     * 获得WMS-批次主档
     *
     * @param id 编号
     * @return WMS-批次主档
     */
    BatchRespVO getBatch(Long id);

    /**
     * 获得WMS-批次主档分页
     *
     * @param pageReqVO 分页查询
     * @return WMS-批次主档分页
     */
    PageResult<BatchRespVO> getBatchPage(BatchPageReqVO pageReqVO);

}