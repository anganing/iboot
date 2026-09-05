package com.iboot.pf.module.mes.api.rework;

import com.iboot.pf.module.mes.api.rework.dto.CreateReworkReqDTO;
import com.iboot.pf.module.mes.api.rework.dto.ReworkRespDTO;

import java.util.Collection;
import java.util.List;

/**
 * 返工任务 API 门面。
 *
 * <p>返工任务由不合格品处置单联动生成，也允许手工发起；返工路线版本按生效版本锁定。
 */
public interface ReworkApi {

    /**
     * 创建返工任务。
     *
     * @param reqDTO 创建命令
     * @return 返工任务编号
     */
    Long createRework(CreateReworkReqDTO reqDTO);

    /**
     * 查询单个返工任务。
     *
     * @param id 返工任务编号
     * @return 返工任务事实，不存在时返回 null
     */
    ReworkRespDTO getRework(Long id);

    /**
     * 批量查询返工任务。
     *
     * @param ids 返工任务编号集合
     * @return 返工任务事实列表
     */
    List<ReworkRespDTO> getReworkList(Collection<Long> ids);

}
