package com.iboot.pf.module.aps.service.scenario;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.aps.controller.admin.scenario.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.scenario.ScenarioDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * APS-排程场景 Service 接口
 *
 * @author iboot
 */
public interface ScenarioService {

    /**
     * 创建APS-排程场景
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createScenario(@Valid ScenarioSaveReqVO createReqVO);

    /**
     * 导入APS-排程场景
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    ScenarioImportRespVO importScenarioList(List<ScenarioImportExcelVO> importList);

    /**
     * 更新APS-排程场景
     *
     * @param updateReqVO 更新信息
     */
    void updateScenario(@Valid ScenarioSaveReqVO updateReqVO);

    /**
     * 删除APS-排程场景
     *
     * @param id 编号
     */
    void deleteScenario(Long id);

    /**
    * 批量删除APS-排程场景
    *
    * @param ids 编号
    */
    void deleteScenarioListByIds(List<Long> ids);

    /**
     * 获得APS-排程场景
     *
     * @param id 编号
     * @return APS-排程场景
     */
    ScenarioRespVO getScenario(Long id);

    /**
     * 获得APS-排程场景分页
     *
     * @param pageReqVO 分页查询
     * @return APS-排程场景分页
     */
    PageResult<ScenarioRespVO> getScenarioPage(ScenarioPageReqVO pageReqVO);

}