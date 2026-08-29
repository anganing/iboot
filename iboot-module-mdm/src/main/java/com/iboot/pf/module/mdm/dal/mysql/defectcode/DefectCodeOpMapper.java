package com.iboot.pf.module.mdm.dal.mysql.defectcode;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.mdm.dal.dataobject.defectcode.DefectCodeOpDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 缺陷代码适用工序 Mapper
 *
 * @author iboot
 */
@Mapper
public interface DefectCodeOpMapper extends BaseMapperX<DefectCodeOpDO> {

    default List<DefectCodeOpDO> selectListByDefectCodeId(Long defectCodeId) {
        return selectList(DefectCodeOpDO::getDefectCodeId, defectCodeId);
    }

    default int deleteByDefectCodeIds(List<Long> defectCodeIds) {
        return deleteBatch(DefectCodeOpDO::getDefectCodeId, defectCodeIds);
    }

}
