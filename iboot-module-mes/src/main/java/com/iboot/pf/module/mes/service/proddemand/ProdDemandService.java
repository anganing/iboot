package com.iboot.pf.module.mes.service.proddemand;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandImportExcelVO;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandImportRespVO;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandPageReqVO;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandRespVO;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.proddemand.ProdDemandDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * MES-生产需求 Service 接口
 *
 * @author iboot
 */
public interface ProdDemandService {

    /**
     * 创建生产需求
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProdDemand(@Valid ProdDemandSaveReqVO createReqVO);

    /**
     * 导入生产需求
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    ProdDemandImportRespVO importProdDemandList(List<ProdDemandImportExcelVO> importList);

    /**
     * 更新生产需求
     *
     * @param updateReqVO 更新信息
     */
    void updateProdDemand(@Valid ProdDemandSaveReqVO updateReqVO);

    /**
     * 删除生产需求
     *
     * @param id 编号
     */
    void deleteProdDemand(Long id);

    /**
     * 批量删除生产需求
     *
     * @param ids 编号
     */
    void deleteProdDemandListByIds(List<Long> ids);

    /**
     * 获得生产需求
     *
     * @param id 编号
     * @return 生产需求
     */
    ProdDemandRespVO getProdDemand(Long id);

    /**
     * 获得生产需求分页
     *
     * @param pageReqVO 分页查询
     * @return 生产需求分页
     */
    PageResult<ProdDemandRespVO> getProdDemandPage(ProdDemandPageReqVO pageReqVO);

    /**
     * 按编号批量获得生产需求，供 Api 门面与本模块内部使用
     *
     * @param ids 编号集合
     * @return 生产需求列表
     */
    List<ProdDemandDO> getProdDemandList(Collection<Long> ids);

}
