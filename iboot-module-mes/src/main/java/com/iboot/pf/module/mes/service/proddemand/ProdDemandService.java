package com.iboot.pf.module.mes.service.proddemand;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.proddemand.ProdDemandDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * MES-生产需求 Service 接口
 *
 * @author iboot
 */
public interface ProdDemandService {

    /**
     * 创建MES-生产需求
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProdDemand(@Valid ProdDemandSaveReqVO createReqVO);

    /**
     * 导入MES-生产需求
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    ProdDemandImportRespVO importProdDemandList(List<ProdDemandImportExcelVO> importList);

    /**
     * 更新MES-生产需求
     *
     * @param updateReqVO 更新信息
     */
    void updateProdDemand(@Valid ProdDemandSaveReqVO updateReqVO);

    /**
     * 删除MES-生产需求
     *
     * @param id 编号
     */
    void deleteProdDemand(Long id);

    /**
    * 批量删除MES-生产需求
    *
    * @param ids 编号
    */
    void deleteProdDemandListByIds(List<Long> ids);

    /**
     * 获得MES-生产需求
     *
     * @param id 编号
     * @return MES-生产需求
     */
    ProdDemandRespVO getProdDemand(Long id);

    /**
     * 获得MES-生产需求分页
     *
     * @param pageReqVO 分页查询
     * @return MES-生产需求分页
     */
    PageResult<ProdDemandRespVO> getProdDemandPage(ProdDemandPageReqVO pageReqVO);

}