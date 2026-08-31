package com.iboot.pf.module.mdm.api.defectcode;

import com.iboot.pf.module.mdm.api.defectcode.dto.DefectCodeRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 缺陷代码 API 接口
 *
 * <p>对 MES 提供缺陷代码与分类门面。
 *
 * @author iboot
 */
public interface DefectCodeApi {

    /**
     * 获得全部缺陷代码
     *
     * @return 缺陷代码列表；无数据返回空列表
     */
    List<DefectCodeRespDTO> getDefectCodeList();

    /**
     * 获得缺陷代码树（只含启用节点）
     *
     * @return 缺陷代码树；无数据返回空列表
     */
    List<DefectCodeRespDTO> getDefectCodeTree();

    /**
     * 按编号批量查询缺陷代码 Map，供批量补全
     *
     * <p>MES 缺陷记录落编码、名称、等级快照时用它取值。
     *
     * @param ids 缺陷代码编号集合
     * @return 缺陷编号 -> 缺陷代码；缺失编号不产生键
     */
    Map<Long, DefectCodeRespDTO> getDefectCodeMap(Collection<Long> ids);

}
