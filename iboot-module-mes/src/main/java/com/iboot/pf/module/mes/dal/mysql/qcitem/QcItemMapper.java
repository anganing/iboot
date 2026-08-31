package com.iboot.pf.module.mes.dal.mysql.qcitem;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.controller.admin.qcitem.vo.QcItemPageReqVO;
import com.iboot.pf.module.mes.controller.admin.qcitem.vo.QcItemRespVO;
import com.iboot.pf.module.mes.dal.dataobject.qcitem.QcItemDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MES-检验项目 Mapper
 *
 * @author iboot
 */
@Mapper
public interface QcItemMapper extends BaseMapperX<QcItemDO> {

    default PageResult<QcItemRespVO> selectPage(QcItemPageReqVO reqVO) {
        IPage<QcItemRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    QcItemRespVO selectJoinById(@Param("id") Long id);

    IPage<QcItemRespVO> selectJoinPage(IPage<QcItemRespVO> page, @Param("req") QcItemPageReqVO reqVO);

    /** 按方案 + 检验项编码查询，唯一索引 uk_mes_qc_item_code_deleted 的应用层前置校验。 */
    default QcItemDO selectByPlanIdAndItemCode(Long planId, String itemCode) {
        return selectOne(QcItemDO::getPlanId, planId, QcItemDO::getItemCode, itemCode);
    }

    default int deleteByPlanId(Long planId) {
        return delete(QcItemDO::getPlanId, planId);
    }

    default int deleteByPlanIds(List<Long> planIds) {
        return deleteBatch(QcItemDO::getPlanId, planIds);
    }

}
