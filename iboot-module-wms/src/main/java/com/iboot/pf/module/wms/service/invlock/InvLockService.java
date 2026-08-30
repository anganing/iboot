package com.iboot.pf.module.wms.service.invlock;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.wms.controller.admin.invlock.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.invlock.InvLockDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * WMS-库存冻结记录 Service 接口
 *
 * @author iboot
 */
public interface InvLockService {

    /**
     * 创建WMS-库存冻结记录
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createInvLock(@Valid InvLockSaveReqVO createReqVO);

    /**
     * 导入WMS-库存冻结记录
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    InvLockImportRespVO importInvLockList(List<InvLockImportExcelVO> importList);

    /**
     * 更新WMS-库存冻结记录
     *
     * @param updateReqVO 更新信息
     */
    void updateInvLock(@Valid InvLockSaveReqVO updateReqVO);

    /**
     * 删除WMS-库存冻结记录
     *
     * @param id 编号
     */
    void deleteInvLock(Long id);

    /**
    * 批量删除WMS-库存冻结记录
    *
    * @param ids 编号
    */
    void deleteInvLockListByIds(List<Long> ids);

    /**
     * 获得WMS-库存冻结记录
     *
     * @param id 编号
     * @return WMS-库存冻结记录
     */
    InvLockRespVO getInvLock(Long id);

    /**
     * 获得WMS-库存冻结记录分页
     *
     * @param pageReqVO 分页查询
     * @return WMS-库存冻结记录分页
     */
    PageResult<InvLockRespVO> getInvLockPage(InvLockPageReqVO pageReqVO);

}