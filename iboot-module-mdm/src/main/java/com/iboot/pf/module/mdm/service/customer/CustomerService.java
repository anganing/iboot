package com.iboot.pf.module.mdm.service.customer;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mdm.controller.admin.customer.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.customer.CustomerDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * 客户 Service 接口
 *
 * @author iboot
 */
public interface CustomerService {

    /**
     * 创建客户
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCustomer(@Valid CustomerSaveReqVO createReqVO);

    /**
     * 更新客户
     *
     * @param updateReqVO 更新信息
     */
    void updateCustomer(@Valid CustomerSaveReqVO updateReqVO);

    /**
     * 删除客户
     *
     * @param id 编号
     */
    void deleteCustomer(Long id);

    /**
    * 批量删除客户
    *
    * @param ids 编号
    */
    void deleteCustomerListByIds(List<Long> ids);

    /**
     * 获得客户
     *
     * @param id 编号
     * @return 客户
     */
    CustomerDO getCustomer(Long id);

    /**
     * 获得客户分页
     *
     * @param pageReqVO 分页查询
     * @return 客户分页
     */
    PageResult<CustomerDO> getCustomerPage(CustomerPageReqVO pageReqVO);

    /**
     * 获得全部客户（含停用）
     *
     * @return 客户列表
     */
    List<CustomerDO> getCustomerList();

    /**
     * 按编号批量查询客户
     *
     * @param ids 客户编号集合
     * @return 客户列表；空集合返回空列表
     */
    List<CustomerDO> getCustomerList(Collection<Long> ids);

}