package com.iboot.pf.module.mes.dal.mysql.defect;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.module.mes.dal.dataobject.defect.DefectFileDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES-缺陷记录附件 Mapper
 *
 * @author iboot
 */
@Mapper
public interface DefectFileMapper extends BaseMapperX<DefectFileDO> {

    default List<DefectFileDO> selectListByDefectId(Long defectId) {
        return selectList(new LambdaQueryWrapperX<DefectFileDO>()
                .eq(DefectFileDO::getDefectId, defectId)
                .orderByAsc(DefectFileDO::getSeq));
    }

    default List<DefectFileDO> selectListByDefectIds(List<Long> defectIds) {
        return selectList(new LambdaQueryWrapperX<DefectFileDO>()
                .in(DefectFileDO::getDefectId, defectIds)
                .orderByAsc(DefectFileDO::getSeq));
    }

    default int deleteByDefectIds(List<Long> defectIds) {
        return deleteBatch(DefectFileDO::getDefectId, defectIds);
    }

}
