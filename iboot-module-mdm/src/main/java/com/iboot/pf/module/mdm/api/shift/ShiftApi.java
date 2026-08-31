package com.iboot.pf.module.mdm.api.shift;

import com.iboot.pf.module.mdm.api.shift.dto.ShiftRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 班次 API 接口
 *
 * <p>对 MES / APS 提供班次门面。派工任务与报工记录都带班次编号，
 * 列表展示班次名称时走这里批量补全。
 *
 * @author iboot
 */
public interface ShiftApi {

    /**
     * 获得全部班次（含停用）
     *
     * @return 班次列表
     */
    List<ShiftRespDTO> getShiftList();

    /**
     * 按编号批量查询班次 Map，供批量补全
     *
     * @param ids 班次编号集合
     * @return 班次编号 -> 班次；缺失编号不产生键
     */
    Map<Long, ShiftRespDTO> getShiftMap(Collection<Long> ids);

}
