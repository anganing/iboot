package com.iboot.pf.module.mdm.service.uomconv;

import java.util.*;

import jakarta.validation.*;
import com.iboot.pf.module.mdm.controller.admin.uomconv.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.uomconv.UomConvDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * 单位换算 Service 接口
 *
 * @author iboot
 */
public interface UomConvService {

    /**
     * 创建单位换算
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createUomConv(@Valid UomConvSaveReqVO createReqVO);

    /**
     * 更新单位换算
     *
     * @param updateReqVO 更新信息
     */
    void updateUomConv(@Valid UomConvSaveReqVO updateReqVO);

    /**
     * 删除单位换算
     *
     * @param id 编号
     */
    void deleteUomConv(Long id);

    /**
     * 批量删除单位换算
     *
     * @param ids 编号
     */
    void deleteUomConvListByIds(List<Long> ids);

    /**
     * 获得单位换算
     *
     * @param id 编号
     * @return 单位换算
     */
    UomConvRespVO getUomConv(Long id);

    /**
     * 获得单位换算分页
     *
     * @param pageReqVO 分页查询
     * @return 单位换算分页
     */
    PageResult<UomConvRespVO> getUomConvPage(UomConvPageReqVO pageReqVO);

}
