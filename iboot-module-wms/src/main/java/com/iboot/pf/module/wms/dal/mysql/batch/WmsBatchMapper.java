package com.iboot.pf.module.wms.dal.mysql.batch;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.module.wms.dal.dataobject.batch.WmsBatchDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.batchmgr.vo.*;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.baomidou.mybatisplus.core.metadata.IPage;

/** 批次主档 Mapper。 */
@Mapper
public interface WmsBatchMapper extends BaseMapperX<WmsBatchDO> {

    /**
     * 按物料与批次号（含序列号）查批次，用于批次注册幂等。
     *
     * @param matId 物料编号
     * @param lotNo 批次号
     * @param snNo  序列号，可为空
     * @return 批次，不存在返回 null
     */
    default WmsBatchDO selectByMatAndLot(Long matId, String lotNo, String snNo) {
        return selectOne(new LambdaQueryWrapperX<WmsBatchDO>()
                .eq(WmsBatchDO::getMatId, matId)
                .eq(WmsBatchDO::getLotNo, lotNo)
                .eq(snNo != null, WmsBatchDO::getSnNo, snNo));
    }

    default PageResult<BatchRespVO> selectPage(BatchPageReqVO reqVO) {
        IPage<BatchRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    BatchRespVO selectJoinById(@Param("id") Long id);

    IPage<BatchRespVO> selectJoinPage(IPage<BatchRespVO> page, @Param("req") BatchPageReqVO reqVO);

    /** 唯一索引的应用层前置校验，命中说明业务编码重复。 */
    default WmsBatchDO selectByMatIdAndLotNo(Long matId, String lotNo) {
        return selectOne(WmsBatchDO::getMatId, matId, WmsBatchDO::getLotNo, lotNo);
    }
}
