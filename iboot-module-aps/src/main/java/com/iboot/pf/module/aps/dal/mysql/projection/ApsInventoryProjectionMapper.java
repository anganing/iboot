package com.iboot.pf.module.aps.dal.mysql.projection;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsInventoryProjectionDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApsInventoryProjectionMapper extends BaseMapperX<ApsInventoryProjectionDO> {
    @Delete("DELETE FROM aps_projection_inv WHERE scenario_id = #{scenarioId}")
    int deletePhysicalByScenarioId(Long scenarioId);
}
