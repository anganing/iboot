package com.iboot.pf.module.mdm.service.res;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mdm.controller.admin.res.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.res.ResDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * 生产资源 Service 接口
 *
 * @author iboot
 */
public interface ResService {

    /**
     * 创建生产资源
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createRes(@Valid ResSaveReqVO createReqVO);

    /**
     * 更新生产资源
     *
     * @param updateReqVO 更新信息
     */
    void updateRes(@Valid ResSaveReqVO updateReqVO);

    /**
     * 删除生产资源
     *
     * @param id 编号
     */
    void deleteRes(Long id);

    /**
    * 批量删除生产资源
    *
    * @param ids 编号
    */
    void deleteResListByIds(List<Long> ids);

    /**
     * 获得生产资源
     *
     * @param id 编号
     * @return 生产资源
     */
    ResDO getRes(Long id);

    /**
     * 获得生产资源分页
     *
     * @param pageReqVO 分页查询
     * @return 生产资源分页
     */
    PageResult<ResDO> getResPage(ResPageReqVO pageReqVO);

}