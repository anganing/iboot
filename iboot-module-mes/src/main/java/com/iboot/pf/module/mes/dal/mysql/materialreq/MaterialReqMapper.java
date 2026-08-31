package com.iboot.pf.module.mes.dal.mysql.materialreq;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqPageReqVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqRespVO;
import com.iboot.pf.module.mes.dal.dataobject.materialreq.MaterialReqDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * MES-领退补料申请 Mapper
 *
 * @author iboot
 */
@Mapper
public interface MaterialReqMapper extends BaseMapperX<MaterialReqDO> {

    default PageResult<MaterialReqRespVO> selectPage(MaterialReqPageReqVO reqVO) {
        IPage<MaterialReqRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    MaterialReqRespVO selectJoinById(@Param("id") Long id);

    IPage<MaterialReqRespVO> selectJoinPage(IPage<MaterialReqRespVO> page, @Param("req") MaterialReqPageReqVO reqVO);

    /** 按申请单号查询，唯一索引 uk_mes_material_req_no_deleted 的应用层前置校验。 */
    default MaterialReqDO selectByReqNo(String reqNo) {
        return selectOne(MaterialReqDO::getReqNo, reqNo);
    }

    /** 按幂等键查询，唯一索引 uk_mes_material_req_idem_deleted 的应用层前置校验。 */
    default MaterialReqDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(MaterialReqDO::getIdempotencyKey, idempotencyKey);
    }

}
