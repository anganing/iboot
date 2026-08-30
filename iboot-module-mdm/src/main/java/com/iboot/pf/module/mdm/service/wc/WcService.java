package com.iboot.pf.module.mdm.service.wc;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mdm.controller.admin.wc.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.wc.WcDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * 工作中心 Service 接口
 *
 * @author iboot
 */
public interface WcService {

    /**
     * 创建工作中心
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createWc(@Valid WcSaveReqVO createReqVO);

    /**
     * 更新工作中心
     *
     * @param updateReqVO 更新信息
     */
    void updateWc(@Valid WcSaveReqVO updateReqVO);

    /**
     * 删除工作中心
     *
     * @param id 编号
     */
    void deleteWc(Long id);

    /**
    * 批量删除工作中心
    *
    * @param ids 编号
    */
    void deleteWcListByIds(List<Long> ids);

    /**
     * 获得工作中心
     *
     * @param id 编号
     * @return 工作中心
     */
    WcRespVO getWc(Long id);

    /**
     * 获得工作中心分页
     *
     * @param pageReqVO 分页查询
     * @return 工作中心分页
     */
    PageResult<WcRespVO> getWcPage(WcPageReqVO pageReqVO);

    /**
     * 获得全部工作中心（含停用）
     *
     * @return 工作中心列表
     */
    List<WcDO> getWcList();

    /**
     * 按车间编号查询其下属工作中心
     *
     * @param workshopId 车间编号
     * @return 工作中心列表；无工作中心返回空列表
     */
    List<WcDO> getWcListByWorkshopId(Long workshopId);

}