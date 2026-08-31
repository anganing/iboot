package com.iboot.pf.module.mes.dal.mysql.materialreqline;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqLinePageReqVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqLineRespVO;
import com.iboot.pf.module.mes.dal.dataobject.materialreqline.MaterialReqLineDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MES-领退补料申请明细 Mapper
 *
 * @author iboot
 */
@Mapper
public interface MaterialReqLineMapper extends BaseMapperX<MaterialReqLineDO> {

    /** 分页查询明细，JOIN 出未落快照的关联字段，前端不做二次拼接。 */
    default PageResult<MaterialReqLineRespVO> selectJoinPage(MaterialReqLinePageReqVO reqVO) {
        IPage<MaterialReqLineRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    MaterialReqLineRespVO selectJoinById(@Param("id") Long id);

    IPage<MaterialReqLineRespVO> selectJoinPage(IPage<MaterialReqLineRespVO> page,
                                               @Param("req") MaterialReqLinePageReqVO reqVO);

    /** 按申请 + 行号查询，唯一索引 uk_mes_material_req_line_deleted 的应用层前置校验。 */
    default MaterialReqLineDO selectByReqIdAndLineNo(Long reqId, Integer lineNo) {
        return selectOne(MaterialReqLineDO::getReqId, reqId, MaterialReqLineDO::getLineNo, lineNo);
    }

    default Long selectCountByReqId(Long reqId) {
        return selectCount(MaterialReqLineDO::getReqId, reqId);
    }

    default int deleteByReqId(Long reqId) {
        return delete(MaterialReqLineDO::getReqId, reqId);
    }

    default int deleteByReqIds(List<Long> reqIds) {
        return deleteBatch(MaterialReqLineDO::getReqId, reqIds);
    }

}
