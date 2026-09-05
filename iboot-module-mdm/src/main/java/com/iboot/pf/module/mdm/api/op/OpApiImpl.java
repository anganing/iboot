package com.iboot.pf.module.mdm.api.op;

import com.iboot.pf.framework.common.util.collection.CollectionUtils;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mdm.api.op.dto.OpRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.op.OpDO;
import com.iboot.pf.module.mdm.dal.mysql.op.OpMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 标准工序 API 实现类
 *
 * @author iboot
 */
@Service
@DataPermission(enable = false)
public class OpApiImpl implements OpApi {

    @Resource
    private OpMapper opMapper;

    @Override
    public OpRespDTO getOp(Long id) {
        OpDO op = id == null ? null : opMapper.selectById(id);
        return BeanUtils.toBean(op, OpRespDTO.class);
    }

    @Override
    public List<OpRespDTO> getOpList() {
        return BeanUtils.toBean(opMapper.selectList(), OpRespDTO.class);
    }

    @Override
    public Map<Long, OpRespDTO> getOpMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        // 一次 IN 查询，避免调用方 N+1
        List<OpDO> list = opMapper.selectByIds(ids);
        return CollectionUtils.convertMap(BeanUtils.toBean(list, OpRespDTO.class), OpRespDTO::getId);
    }

}
