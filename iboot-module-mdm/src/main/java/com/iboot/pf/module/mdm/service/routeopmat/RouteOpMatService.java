package com.iboot.pf.module.mdm.service.routeopmat;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mdm.controller.admin.routeopmat.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.routeopmat.RouteOpMatDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * 工序用料 Service 接口
 *
 * @author iboot
 */
public interface RouteOpMatService {

    /**
     * 创建工序用料
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createRouteOpMat(@Valid RouteOpMatSaveReqVO createReqVO);

    /**
     * 更新工序用料
     *
     * @param updateReqVO 更新信息
     */
    void updateRouteOpMat(@Valid RouteOpMatSaveReqVO updateReqVO);

    /**
     * 删除工序用料
     *
     * @param id 编号
     */
    void deleteRouteOpMat(Long id);

    /**
    * 批量删除工序用料
    *
    * @param ids 编号
    */
    void deleteRouteOpMatListByIds(List<Long> ids);

    /**
     * 获得工序用料
     *
     * @param id 编号
     * @return 工序用料
     */
    RouteOpMatDO getRouteOpMat(Long id);

    /**
     * 获得工序用料分页
     *
     * @param pageReqVO 分页查询
     * @return 工序用料分页
     */
    PageResult<RouteOpMatDO> getRouteOpMatPage(RouteOpMatPageReqVO pageReqVO);

}