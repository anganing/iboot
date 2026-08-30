package com.iboot.pf.module.mdm.api.partner;

import com.iboot.pf.module.mdm.api.partner.dto.CustomerRespDTO;
import com.iboot.pf.module.mdm.api.partner.dto.SupplierRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 供应商与客户 API 接口
 *
 * <p>对 WMS / MES 提供供应商与客户门面。
 *
 * @author iboot
 */
public interface PartnerApi {

    /**
     * 获得全部供应商（含停用）
     *
     * @return 供应商列表
     */
    List<SupplierRespDTO> getSupplierList();

    /**
     * 按编号批量查询供应商 Map，供批量补全
     *
     * @param ids 供应商编号集合
     * @return 供应商编号 -> 供应商；缺失编号不产生键
     */
    Map<Long, SupplierRespDTO> getSupplierMap(Collection<Long> ids);

    /**
     * 获得全部客户（含停用）
     *
     * @return 客户列表
     */
    List<CustomerRespDTO> getCustomerList();

    /**
     * 按编号批量查询客户 Map，供批量补全
     *
     * @param ids 客户编号集合
     * @return 客户编号 -> 客户；缺失编号不产生键
     */
    Map<Long, CustomerRespDTO> getCustomerMap(Collection<Long> ids);

}
