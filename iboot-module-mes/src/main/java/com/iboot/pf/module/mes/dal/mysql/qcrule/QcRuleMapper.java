package com.iboot.pf.module.mes.dal.mysql.qcrule;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.controller.admin.qcrule.vo.QcRulePageReqVO;
import com.iboot.pf.module.mes.controller.admin.qcrule.vo.QcRuleRespVO;
import com.iboot.pf.module.mes.dal.dataobject.qcrule.QcRuleDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * MES-质检触发规则 Mapper
 *
 * @author iboot
 */
@Mapper
public interface QcRuleMapper extends BaseMapperX<QcRuleDO> {

    default PageResult<QcRuleRespVO> selectPage(QcRulePageReqVO reqVO) {
        IPage<QcRuleRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    QcRuleRespVO selectJoinById(@Param("id") Long id);

    IPage<QcRuleRespVO> selectJoinPage(IPage<QcRuleRespVO> page, @Param("req") QcRulePageReqVO reqVO);

    /** 按规则编码查询，唯一索引 uk_mes_qc_rule_code_deleted 的应用层前置校验。 */
    default QcRuleDO selectByCode(String code) {
        return selectOne(QcRuleDO::getCode, code);
    }

    /** 统计引用某检验方案的规则数，方案删除前校验占用。 */
    default Long selectCountByQcPlanId(Long qcPlanId) {
        return selectCount(QcRuleDO::getQcPlanId, qcPlanId);
    }

}
