package com.iboot.pf.module.mdm.service.supplier;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mdm.controller.admin.supplier.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.supplier.SupplierDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * 供应商 Service 接口
 *
 * @author iboot
 */
public interface SupplierService {

    /**
     * 创建供应商
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSupplier(@Valid SupplierSaveReqVO createReqVO);

    /**
     * 更新供应商
     *
     * @param updateReqVO 更新信息
     */
    void updateSupplier(@Valid SupplierSaveReqVO updateReqVO);

    /**
     * 删除供应商
     *
     * @param id 编号
     */
    void deleteSupplier(Long id);

    /**
    * 批量删除供应商
    *
    * @param ids 编号
    */
    void deleteSupplierListByIds(List<Long> ids);

    /**
     * 获得供应商
     *
     * @param id 编号
     * @return 供应商
     */
    SupplierDO getSupplier(Long id);

    /**
     * 获得供应商分页
     *
     * @param pageReqVO 分页查询
     * @return 供应商分页
     */
    PageResult<SupplierDO> getSupplierPage(SupplierPageReqVO pageReqVO);

    /**
     * 获得全部供应商（含停用）
     *
     * @return 供应商列表
     */
    List<SupplierDO> getSupplierList();

    /**
     * 按编号批量查询供应商
     *
     * @param ids 供应商编号集合
     * @return 供应商列表；空集合返回空列表
     */
    List<SupplierDO> getSupplierList(Collection<Long> ids);

}