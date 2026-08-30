package com.iboot.pf.module.mes.service.wiptxn;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mes.controller.admin.wiptxn.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.wiptxn.WipTxnDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * MES-在制流转流水 Service 接口
 *
 * @author iboot
 */
public interface WipTxnService {

    /**
     * 创建MES-在制流转流水
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createWipTxn(@Valid WipTxnSaveReqVO createReqVO);

    /**
     * 导入MES-在制流转流水
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    WipTxnImportRespVO importWipTxnList(List<WipTxnImportExcelVO> importList);

    /**
     * 更新MES-在制流转流水
     *
     * @param updateReqVO 更新信息
     */
    void updateWipTxn(@Valid WipTxnSaveReqVO updateReqVO);

    /**
     * 删除MES-在制流转流水
     *
     * @param id 编号
     */
    void deleteWipTxn(Long id);

    /**
    * 批量删除MES-在制流转流水
    *
    * @param ids 编号
    */
    void deleteWipTxnListByIds(List<Long> ids);

    /**
     * 获得MES-在制流转流水
     *
     * @param id 编号
     * @return MES-在制流转流水
     */
    WipTxnRespVO getWipTxn(Long id);

    /**
     * 获得MES-在制流转流水分页
     *
     * @param pageReqVO 分页查询
     * @return MES-在制流转流水分页
     */
    PageResult<WipTxnRespVO> getWipTxnPage(WipTxnPageReqVO pageReqVO);

}