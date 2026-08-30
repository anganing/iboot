package com.iboot.pf.module.mes.service.qcrule;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mes.controller.admin.qcrule.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.qcrule.QcRuleDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * MES-质检触发规则 Service 接口
 *
 * @author iboot
 */
public interface QcRuleService {

    /**
     * 创建MES-质检触发规则
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createQcRule(@Valid QcRuleSaveReqVO createReqVO);

    /**
     * 导入MES-质检触发规则
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    QcRuleImportRespVO importQcRuleList(List<QcRuleImportExcelVO> importList);

    /**
     * 更新MES-质检触发规则
     *
     * @param updateReqVO 更新信息
     */
    void updateQcRule(@Valid QcRuleSaveReqVO updateReqVO);

    /**
     * 删除MES-质检触发规则
     *
     * @param id 编号
     */
    void deleteQcRule(Long id);

    /**
    * 批量删除MES-质检触发规则
    *
    * @param ids 编号
    */
    void deleteQcRuleListByIds(List<Long> ids);

    /**
     * 获得MES-质检触发规则
     *
     * @param id 编号
     * @return MES-质检触发规则
     */
    QcRuleRespVO getQcRule(Long id);

    /**
     * 获得MES-质检触发规则分页
     *
     * @param pageReqVO 分页查询
     * @return MES-质检触发规则分页
     */
    PageResult<QcRuleRespVO> getQcRulePage(QcRulePageReqVO pageReqVO);

}