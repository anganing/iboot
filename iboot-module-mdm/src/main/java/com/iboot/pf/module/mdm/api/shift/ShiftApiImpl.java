package com.iboot.pf.module.mdm.api.shift;

import com.iboot.pf.framework.common.util.collection.CollectionUtils;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mdm.api.shift.dto.ShiftRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.shift.ShiftDO;
import com.iboot.pf.module.mdm.dal.mysql.shift.ShiftMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 班次 API 实现类
 *
 * @author iboot
 */
@Service
@DataPermission(enable = false)
public class ShiftApiImpl implements ShiftApi {

    @Resource
    private ShiftMapper shiftMapper;

    @Override
    public List<ShiftRespDTO> getShiftList() {
        return BeanUtils.toBean(shiftMapper.selectList(), ShiftRespDTO.class);
    }

    @Override
    public Map<Long, ShiftRespDTO> getShiftMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        // 一次 IN 查询，避免调用方 N+1
        List<ShiftDO> list = shiftMapper.selectByIds(ids);
        return CollectionUtils.convertMap(BeanUtils.toBean(list, ShiftRespDTO.class), ShiftRespDTO::getId);
    }

}
