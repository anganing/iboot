package com.iboot.pf.module.mdm.service.shift;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mdm.controller.admin.shift.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.shift.ShiftDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * 班次 Service 接口
 *
 * @author iboot
 */
public interface ShiftService {

    /**
     * 创建班次
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createShift(@Valid ShiftSaveReqVO createReqVO);

    /**
     * 更新班次
     *
     * @param updateReqVO 更新信息
     */
    void updateShift(@Valid ShiftSaveReqVO updateReqVO);

    /**
     * 删除班次
     *
     * @param id 编号
     */
    void deleteShift(Long id);

    /**
    * 批量删除班次
    *
    * @param ids 编号
    */
    void deleteShiftListByIds(List<Long> ids);

    /**
     * 获得班次
     *
     * @param id 编号
     * @return 班次
     */
    ShiftDO getShift(Long id);

    /**
     * 获得班次分页
     *
     * @param pageReqVO 分页查询
     * @return 班次分页
     */
    PageResult<ShiftDO> getShiftPage(ShiftPageReqVO pageReqVO);

}