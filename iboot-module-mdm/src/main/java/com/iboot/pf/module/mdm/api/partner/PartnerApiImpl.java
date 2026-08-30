package com.iboot.pf.module.mdm.api.partner;

import com.iboot.pf.framework.common.util.collection.CollectionUtils;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mdm.api.partner.dto.CustomerRespDTO;
import com.iboot.pf.module.mdm.api.partner.dto.SupplierRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.customer.CustomerDO;
import com.iboot.pf.module.mdm.dal.dataobject.supplier.SupplierDO;
import com.iboot.pf.module.mdm.service.customer.CustomerService;
import com.iboot.pf.module.mdm.service.supplier.SupplierService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 供应商与客户 API 实现类
 *
 * @author iboot
 */
@Service
public class PartnerApiImpl implements PartnerApi {

    @Resource
    private SupplierService supplierService;
    @Resource
    private CustomerService customerService;

    @Override
    @DataPermission(enable = false)
    public List<SupplierRespDTO> getSupplierList() {
        List<SupplierDO> list = supplierService.getSupplierList();
        return BeanUtils.toBean(list, SupplierRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public Map<Long, SupplierRespDTO> getSupplierMap(Collection<Long> ids) {
        List<SupplierDO> list = supplierService.getSupplierList(ids);
        List<SupplierRespDTO> dtos = BeanUtils.toBean(list, SupplierRespDTO.class);
        return CollectionUtils.convertMap(dtos, SupplierRespDTO::getId);
    }

    @Override
    @DataPermission(enable = false)
    public List<CustomerRespDTO> getCustomerList() {
        List<CustomerDO> list = customerService.getCustomerList();
        return BeanUtils.toBean(list, CustomerRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public Map<Long, CustomerRespDTO> getCustomerMap(Collection<Long> ids) {
        List<CustomerDO> list = customerService.getCustomerList(ids);
        List<CustomerRespDTO> dtos = BeanUtils.toBean(list, CustomerRespDTO.class);
        return CollectionUtils.convertMap(dtos, CustomerRespDTO::getId);
    }

}
