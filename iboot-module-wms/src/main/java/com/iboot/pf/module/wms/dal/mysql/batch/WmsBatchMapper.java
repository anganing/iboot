package com.iboot.pf.module.wms.dal.mysql.batch;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.module.wms.dal.dataobject.batch.WmsBatchDO;
import org.apache.ibatis.annotations.Mapper;

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

}
