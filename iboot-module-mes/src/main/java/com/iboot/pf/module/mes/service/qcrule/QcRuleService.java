package com.iboot.pf.module.mes.service.qcrule;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mes.controller.admin.qcrule.vo.QcRulePageReqVO;
import com.iboot.pf.module.mes.controller.admin.qcrule.vo.QcRuleRespVO;
import com.iboot.pf.module.mes.controller.admin.qcrule.vo.QcRuleSaveReqVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES-质检触发规则 Service 接口
 *
 * @author iboot
 */
public interface QcRuleService {

    /**
     * 创建质检触发规则
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createQcRule(@Valid QcRuleSaveReqVO createReqVO);

    /**
     * 更新质检触发规则
     *
     * @param updateReqVO 更新信息
     */
    void updateQcRule(@Valid QcRuleSaveReqVO updateReqVO);

    /**
     * 删除质检触发规则
     *
     * @param id 编号
     */
    void deleteQcRule(Long id);

    /**
     * 批量删除质检触发规则
     *
     * @param ids 编号
     */
    void deleteQcRuleListByIds(List<Long> ids);

    /**
     * 获得质检触发规则
     *
     * @param id 编号
     * @return 质检触发规则
     */
    QcRuleRespVO getQcRule(Long id);

    /**
     * 获得质检触发规则分页
     *
     * @param pageReqVO 分页查询
     * @return 质检触发规则分页
     */
    PageResult<QcRuleRespVO> getQcRulePage(QcRulePageReqVO pageReqVO);

}
