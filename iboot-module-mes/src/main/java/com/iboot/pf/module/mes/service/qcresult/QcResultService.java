package com.iboot.pf.module.mes.service.qcresult;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mes.controller.admin.qcresult.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.qcresult.QcResultDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * MES-检验结果 Service 接口
 *
 * @author iboot
 */
public interface QcResultService {

    /**
     * 创建MES-检验结果
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createQcResult(@Valid QcResultSaveReqVO createReqVO);

    /**
     * 导入MES-检验结果
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    QcResultImportRespVO importQcResultList(List<QcResultImportExcelVO> importList);

    /**
     * 更新MES-检验结果
     *
     * @param updateReqVO 更新信息
     */
    void updateQcResult(@Valid QcResultSaveReqVO updateReqVO);

    /**
     * 删除MES-检验结果
     *
     * @param id 编号
     */
    void deleteQcResult(Long id);

    /**
    * 批量删除MES-检验结果
    *
    * @param ids 编号
    */
    void deleteQcResultListByIds(List<Long> ids);

    /**
     * 获得MES-检验结果
     *
     * @param id 编号
     * @return MES-检验结果
     */
    QcResultRespVO getQcResult(Long id);

    /**
     * 获得MES-检验结果分页
     *
     * @param pageReqVO 分页查询
     * @return MES-检验结果分页
     */
    PageResult<QcResultRespVO> getQcResultPage(QcResultPageReqVO pageReqVO);

}