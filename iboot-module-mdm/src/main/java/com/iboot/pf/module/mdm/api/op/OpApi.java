package com.iboot.pf.module.mdm.api.op;

import com.iboot.pf.module.mdm.api.op.dto.OpRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 标准工序 API 接口
 *
 * <p>对 MES / APS 提供标准工序门面。MES 的工序任务需要落工序编码、名称快照，
 * 一次 IN 查询批量取回，不允许跨库 JOIN mdm_op。
 *
 * @author iboot
 */
public interface OpApi {

    /**
     * 按编号单查标准工序
     *
     * @param id 标准工序编号
     * @return 标准工序；不存在返回 {@code null}
     */
    OpRespDTO getOp(Long id);

    /**
     * 获得全部标准工序（含停用）
     *
     * @return 标准工序列表
     */
    List<OpRespDTO> getOpList();

    /**
     * 按编号批量查询标准工序 Map，供批量补全
     *
     * @param ids 标准工序编号集合
     * @return 工序编号 -> 工序；缺失编号不产生键
     */
    Map<Long, OpRespDTO> getOpMap(Collection<Long> ids);

}
