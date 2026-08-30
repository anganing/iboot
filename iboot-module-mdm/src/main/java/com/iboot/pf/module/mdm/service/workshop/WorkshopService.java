package com.iboot.pf.module.mdm.service.workshop;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mdm.controller.admin.workshop.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.workshop.WorkshopDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * 车间 Service 接口
 *
 * @author iboot
 */
public interface WorkshopService {

    /**
     * 创建车间
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createWorkshop(@Valid WorkshopSaveReqVO createReqVO);

    /**
     * 更新车间
     *
     * @param updateReqVO 更新信息
     */
    void updateWorkshop(@Valid WorkshopSaveReqVO updateReqVO);

    /**
     * 删除车间
     *
     * @param id 编号
     */
    void deleteWorkshop(Long id);

    /**
    * 批量删除车间
    *
    * @param ids 编号
    */
    void deleteWorkshopListByIds(List<Long> ids);

    /**
     * 获得车间
     *
     * @param id 编号
     * @return 车间
     */
    WorkshopRespVO getWorkshop(Long id);

    /**
     * 获得车间分页
     *
     * @param pageReqVO 分页查询
     * @return 车间分页
     */
    PageResult<WorkshopRespVO> getWorkshopPage(WorkshopPageReqVO pageReqVO);

    /**
     * 获得全部车间（含停用）
     *
     * @return 车间列表
     */
    List<WorkshopDO> getWorkshopList();

    /**
     * 按工厂编号查询其下属车间
     *
     * @param plantId 工厂编号
     * @return 车间列表；无车间返回空列表
     */
    List<WorkshopDO> getWorkshopListByPlantId(Long plantId);

}