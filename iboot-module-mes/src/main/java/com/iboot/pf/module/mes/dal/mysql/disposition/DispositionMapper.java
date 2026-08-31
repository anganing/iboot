package com.iboot.pf.module.mes.dal.mysql.disposition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.controller.admin.disposition.vo.DispositionPageReqVO;
import com.iboot.pf.module.mes.controller.admin.disposition.vo.DispositionRespVO;
import com.iboot.pf.module.mes.dal.dataobject.disposition.DispositionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * MES-不合格品处置单 Mapper
 *
 * @author iboot
 */
@Mapper
public interface DispositionMapper extends BaseMapperX<DispositionDO> {

    default PageResult<DispositionRespVO> selectPage(DispositionPageReqVO reqVO) {
        IPage<DispositionRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    DispositionRespVO selectJoinById(@Param("id") Long id);

    IPage<DispositionRespVO> selectJoinPage(IPage<DispositionRespVO> page, @Param("req") DispositionPageReqVO reqVO);

    /** 按处置单号查询，唯一索引 uk_mes_disposition_no_deleted 的应用层前置校验。 */
    default DispositionDO selectByDispositionNo(String dispositionNo) {
        return selectOne(DispositionDO::getDispositionNo, dispositionNo);
    }

    /** 按幂等键查询，唯一索引 uk_mes_disposition_idem_deleted 的应用层前置校验。 */
    default DispositionDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(DispositionDO::getIdempotencyKey, idempotencyKey);
    }

}
