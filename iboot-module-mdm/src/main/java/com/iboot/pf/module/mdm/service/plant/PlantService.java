package com.iboot.pf.module.mdm.service.plant;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mdm.controller.admin.plant.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.plant.PlantDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * 工厂 Service 接口
 *
 * @author iboot
 */
public interface PlantService {

    /**
     * 创建工厂
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPlant(@Valid PlantSaveReqVO createReqVO);

    /**
     * 更新工厂
     *
     * @param updateReqVO 更新信息
     */
    void updatePlant(@Valid PlantSaveReqVO updateReqVO);

    /**
     * 删除工厂
     *
     * @param id 编号
     */
    void deletePlant(Long id);

    /**
    * 批量删除工厂
    *
    * @param ids 编号
    */
    void deletePlantListByIds(List<Long> ids);

    /**
     * 获得工厂
     *
     * @param id 编号
     * @return 工厂
     */
    PlantDO getPlant(Long id);

    /**
     * 获得工厂分页
     *
     * @param pageReqVO 分页查询
     * @return 工厂分页
     */
    PageResult<PlantDO> getPlantPage(PlantPageReqVO pageReqVO);

}