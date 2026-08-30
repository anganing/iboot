package com.iboot.pf.module.mdm.api.material;

import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 物料主档 API 接口
 *
 * <p>对 WMS / MES / APS 提供只读的物料主档门面。
 *
 * @author iboot
 */
public interface MaterialApi {

    /**
     * 按编号单查物料主档
     *
     * @param id 物料编号
     * @return 物料主档；不存在返回 {@code null}
     */
    MaterialRespDTO getMaterial(Long id);

    /**
     * 按编号批量查询物料主档
     *
     * @param ids 物料编号集合
     * @return 物料主档列表；空集合返回空列表，不抛异常
     */
    List<MaterialRespDTO> getMaterialList(Collection<Long> ids);

    /**
     * 按编号批量查询物料主档 Map，供批量补全，避免 N+1
     *
     * @param ids 物料编号集合
     * @return 物料编号 -> 物料主档；缺失编号不产生键
     */
    Map<Long, MaterialRespDTO> getMaterialMap(Collection<Long> ids);

    /**
     * 按编码批量查询物料主档
     *
     * @param codes 物料编码集合
     * @return 物料主档列表；空集合返回空列表，不抛异常
     */
    List<MaterialRespDTO> getMaterialListByCodes(Collection<String> codes);

    /**
     * 校验物料编号集合是否均存在且启用
     *
     * @param ids 物料编号集合
     */
    void validateMaterialList(Collection<Long> ids);

}
