package com.iboot.pf.module.mes.service.qcplan;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mes.controller.admin.qcplan.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.qcplan.QcPlanDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * MES-检验方案 Service 接口
 *
 * @author iboot
 */
public interface QcPlanService {

    /**
     * 创建MES-检验方案
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createQcPlan(@Valid QcPlanSaveReqVO createReqVO);

    /**
     * 导入MES-检验方案
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    QcPlanImportRespVO importQcPlanList(List<QcPlanImportExcelVO> importList);

    /**
     * 更新MES-检验方案
     *
     * @param updateReqVO 更新信息
     */
    void updateQcPlan(@Valid QcPlanSaveReqVO updateReqVO);

    /**
     * 删除MES-检验方案
     *
     * @param id 编号
     */
    void deleteQcPlan(Long id);

    /**
    * 批量删除MES-检验方案
    *
    * @param ids 编号
    */
    void deleteQcPlanListByIds(List<Long> ids);

    /**
     * 获得MES-检验方案
     *
     * @param id 编号
     * @return MES-检验方案
     */
    QcPlanRespVO getQcPlan(Long id);

    /**
     * 获得MES-检验方案分页
     *
     * @param pageReqVO 分页查询
     * @return MES-检验方案分页
     */
    PageResult<QcPlanRespVO> getQcPlanPage(QcPlanPageReqVO pageReqVO);

}