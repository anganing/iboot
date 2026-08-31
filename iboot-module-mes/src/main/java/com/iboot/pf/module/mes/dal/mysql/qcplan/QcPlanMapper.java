package com.iboot.pf.module.mes.dal.mysql.qcplan;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.controller.admin.qcplan.vo.QcPlanPageReqVO;
import com.iboot.pf.module.mes.controller.admin.qcplan.vo.QcPlanRespVO;
import com.iboot.pf.module.mes.dal.dataobject.qcplan.QcPlanDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * MES-检验方案 Mapper
 *
 * @author iboot
 */
@Mapper
public interface QcPlanMapper extends BaseMapperX<QcPlanDO> {

    default PageResult<QcPlanRespVO> selectPage(QcPlanPageReqVO reqVO) {
        IPage<QcPlanRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    QcPlanRespVO selectJoinById(@Param("id") Long id);

    IPage<QcPlanRespVO> selectJoinPage(IPage<QcPlanRespVO> page, @Param("req") QcPlanPageReqVO reqVO);

    /** 按编码 + 版本查询，唯一索引 uk_mes_qc_plan_code_ver_deleted 的应用层前置校验。 */
    default QcPlanDO selectByCodeAndVersion(String code, String versionNo) {
        return selectOne(QcPlanDO::getCode, code, QcPlanDO::getVersionNo, versionNo);
    }

}
