package com.iboot.pf.module.mes.service.prodreport;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mes.controller.admin.prodreport.vo.ProdReportPageReqVO;
import com.iboot.pf.module.mes.controller.admin.prodreport.vo.ProdReportRespVO;
import com.iboot.pf.module.mes.controller.admin.prodreport.vo.ProdReportSaveReqVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES-生产报工记录 Service 接口
 *
 * @author iboot
 */
public interface ProdReportService {

    /**
     * 创建报工记录
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProdReport(@Valid ProdReportSaveReqVO createReqVO);

    /**
     * 更新报工记录
     *
     * @param updateReqVO 更新信息
     */
    void updateProdReport(@Valid ProdReportSaveReqVO updateReqVO);

    /**
     * 删除报工记录
     *
     * @param id 编号
     */
    void deleteProdReport(Long id);

    /**
     * 批量删除报工记录
     *
     * @param ids 编号
     */
    void deleteProdReportListByIds(List<Long> ids);

    /**
     * 获得报工记录
     *
     * @param id 编号
     * @return 报工记录
     */
    ProdReportRespVO getProdReport(Long id);

    /**
     * 获得报工记录分页
     *
     * @param pageReqVO 分页查询
     * @return 报工记录分页
     */
    PageResult<ProdReportRespVO> getProdReportPage(ProdReportPageReqVO pageReqVO);

}
