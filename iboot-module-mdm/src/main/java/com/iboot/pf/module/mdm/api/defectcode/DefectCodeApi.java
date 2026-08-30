package com.iboot.pf.module.mdm.api.defectcode;

import com.iboot.pf.module.mdm.api.defectcode.dto.DefectCodeRespDTO;

import java.util.List;

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

}
