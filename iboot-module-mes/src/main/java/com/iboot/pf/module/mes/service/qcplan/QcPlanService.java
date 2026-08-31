package com.iboot.pf.module.mes.service.qcplan;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mes.controller.admin.qcplan.vo.QcPlanPageReqVO;
import com.iboot.pf.module.mes.controller.admin.qcplan.vo.QcPlanRespVO;
import com.iboot.pf.module.mes.controller.admin.qcplan.vo.QcPlanSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.qcplan.QcPlanDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES-检验方案 Service 接口
 *
 * @author iboot
 */
public interface QcPlanService {

    /**
     * 创建检验方案
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createQcPlan(@Valid QcPlanSaveReqVO createReqVO);

    /**
     * 更新检验方案
     *
     * @param updateReqVO 更新信息
     */
    void updateQcPlan(@Valid QcPlanSaveReqVO updateReqVO);

    /**
     * 删除检验方案
     *
     * @param id 编号
     */
    void deleteQcPlan(Long id);

    /**
     * 批量删除检验方案
     *
     * @param ids 编号
     */
    void deleteQcPlanListByIds(List<Long> ids);

    /**
     * 获得检验方案
     *
     * @param id 编号
     * @return 检验方案
     */
    QcPlanRespVO getQcPlan(Long id);

    /**
     * 获得检验方案分页
     *
     * @param pageReqVO 分页查询
     * @return 检验方案分页
     */
    PageResult<QcPlanRespVO> getQcPlanPage(QcPlanPageReqVO pageReqVO);

    /**
     * 校验检验方案存在并返回，供检验项、质检规则、质检任务引用时使用
     *
     * @param id 编号
     * @return 检验方案
     */
    QcPlanDO validateQcPlanExists(Long id);

}
