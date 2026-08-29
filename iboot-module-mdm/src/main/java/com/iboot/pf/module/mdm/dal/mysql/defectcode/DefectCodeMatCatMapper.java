package com.iboot.pf.module.mdm.dal.mysql.defectcode;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.mdm.dal.dataobject.defectcode.DefectCodeMatCatDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 缺陷代码适用物料分类 Mapper
 *
 * @author iboot
 */
@Mapper
public interface DefectCodeMatCatMapper extends BaseMapperX<DefectCodeMatCatDO> {

    default List<DefectCodeMatCatDO> selectListByDefectCodeId(Long defectCodeId) {
        return selectList(DefectCodeMatCatDO::getDefectCodeId, defectCodeId);
    }

    default int deleteByDefectCodeIds(List<Long> defectCodeIds) {
        return deleteBatch(DefectCodeMatCatDO::getDefectCodeId, defectCodeIds);
    }

}
