package com.iboot.pf.module.mdm.api.uom;

import com.iboot.pf.module.mdm.api.uom.dto.UomConvRespDTO;
import com.iboot.pf.module.mdm.api.uom.dto.UomRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 计量单位与换算 API 接口
 *
 * <p>对 WMS / MES / APS 提供计量单位与单位换算门面。
 *
 * @author iboot
 */
public interface UomApi {

    /**
     * 获得全部计量单位（含停用）
     *
     * @return 计量单位列表；无数据返回空列表
     */
    List<UomRespDTO> getUomList();

    /**
     * 按编号批量查询计量单位 Map，供批量补全
     *
     * @param ids 计量单位编号集合
     * @return 计量单位编号 -> 计量单位；缺失编号不产生键
     */
    Map<Long, UomRespDTO> getUomMap(Collection<Long> ids);

    /**
     * 按物料编号批量查询单位换算
     *
     * @param matIds 物料编号集合
     * @return 单位换算列表；空集合返回空列表
     */
    List<UomConvRespDTO> getUomConvList(Collection<Long> matIds);

}
