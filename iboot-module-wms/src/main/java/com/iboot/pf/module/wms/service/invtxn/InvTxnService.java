package com.iboot.pf.module.wms.service.invtxn;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.wms.controller.admin.invtxn.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.invtxn.InvTxnDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * WMS-库存流水 Service 接口
 *
 * @author iboot
 */
public interface InvTxnService {

    /**
     * 创建WMS-库存流水
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createInvTxn(@Valid InvTxnSaveReqVO createReqVO);

    /**
     * 导入WMS-库存流水
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    InvTxnImportRespVO importInvTxnList(List<InvTxnImportExcelVO> importList);

    /**
     * 更新WMS-库存流水
     *
     * @param updateReqVO 更新信息
     */
    void updateInvTxn(@Valid InvTxnSaveReqVO updateReqVO);

    /**
     * 删除WMS-库存流水
     *
     * @param id 编号
     */
    void deleteInvTxn(Long id);

    /**
    * 批量删除WMS-库存流水
    *
    * @param ids 编号
    */
    void deleteInvTxnListByIds(List<Long> ids);

    /**
     * 获得WMS-库存流水
     *
     * @param id 编号
     * @return WMS-库存流水
     */
    InvTxnRespVO getInvTxn(Long id);

    /**
     * 获得WMS-库存流水分页
     *
     * @param pageReqVO 分页查询
     * @return WMS-库存流水分页
     */
    PageResult<InvTxnRespVO> getInvTxnPage(InvTxnPageReqVO pageReqVO);

}