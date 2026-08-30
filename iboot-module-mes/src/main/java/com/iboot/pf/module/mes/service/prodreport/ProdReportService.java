package com.iboot.pf.module.mes.service.prodreport;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mes.controller.admin.prodreport.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.prodreport.ProdReportDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * MES-生产报工记录 Service 接口
 *
 * @author iboot
 */
public interface ProdReportService {

    /**
     * 创建MES-生产报工记录
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProdReport(@Valid ProdReportSaveReqVO createReqVO);

    /**
     * 导入MES-生产报工记录
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    ProdReportImportRespVO importProdReportList(List<ProdReportImportExcelVO> importList);

    /**
     * 更新MES-生产报工记录
     *
     * @param updateReqVO 更新信息
     */
    void updateProdReport(@Valid ProdReportSaveReqVO updateReqVO);

    /**
     * 删除MES-生产报工记录
     *
     * @param id 编号
     */
    void deleteProdReport(Long id);

    /**
    * 批量删除MES-生产报工记录
    *
    * @param ids 编号
    */
    void deleteProdReportListByIds(List<Long> ids);

    /**
     * 获得MES-生产报工记录
     *
     * @param id 编号
     * @return MES-生产报工记录
     */
    ProdReportRespVO getProdReport(Long id);

    /**
     * 获得MES-生产报工记录分页
     *
     * @param pageReqVO 分页查询
     * @return MES-生产报工记录分页
     */
    PageResult<ProdReportRespVO> getProdReportPage(ProdReportPageReqVO pageReqVO);

}