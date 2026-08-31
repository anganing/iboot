package com.iboot.pf.module.aps.service.scenario;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.aps.api.scenario.dto.CloseScenarioReqDTO;
import com.iboot.pf.module.aps.api.scenario.dto.CopyScenarioReqDTO;
import com.iboot.pf.module.aps.api.scenario.dto.CreateScenarioReqDTO;
import com.iboot.pf.module.aps.controller.admin.scenario.vo.ScenarioPageReqVO;
import com.iboot.pf.module.aps.controller.admin.scenario.vo.ScenarioRespVO;
import com.iboot.pf.module.aps.controller.admin.scenario.vo.ScenarioSaveReqVO;
import com.iboot.pf.module.aps.dal.dataobject.scenario.ApsScenarioDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * APS-排程场景 Service 接口。
 *
 * <p>管理后台 CRUD 与跨模块命令合在同一个 Service：两者写的是同一张表、同一套状态机，
 * 拆成两个 Service 会出现两份校验逻辑，迟早不一致。
 *
 * @author iboot
 */
public interface ApsScenarioService {

    /**
     * 创建排程场景
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createScenario(@Valid ScenarioSaveReqVO createReqVO);

    /**
     * 更新排程场景
     *
     * @param updateReqVO 更新信息
     */
    void updateScenario(@Valid ScenarioSaveReqVO updateReqVO);

    /**
     * 删除排程场景
     *
     * @param id 编号
     */
    void deleteScenario(Long id);

    /**
     * 批量删除排程场景
     *
     * @param ids 编号
     */
    void deleteScenarioListByIds(List<Long> ids);

    /**
     * 获得排程场景
     *
     * @param id 编号
     * @return 排程场景
     */
    ScenarioRespVO getScenario(Long id);

    /**
     * 获得排程场景分页
     *
     * @param pageReqVO 分页查询
     * @return 排程场景分页
     */
    PageResult<ScenarioRespVO> getScenarioPage(ScenarioPageReqVO pageReqVO);

    /**
     * 跨模块命令：创建排程场景
     *
     * @param reqDTO 命令
     * @return 场景编号
     */
    Long createScenario(@Valid CreateScenarioReqDTO reqDTO);

    /**
     * 跨模块命令：复制排程场景
     *
     * @param reqDTO 命令
     * @return 新场景编号
     */
    Long copyScenario(@Valid CopyScenarioReqDTO reqDTO);

    /**
     * 跨模块命令：关闭排程场景
     *
     * @param reqDTO 命令
     */
    void closeScenario(@Valid CloseScenarioReqDTO reqDTO);

    /**
     * 获得排程场景 DO，供模块内与 Api 门面使用
     *
     * @param id 编号
     * @return 排程场景；不存在返回 {@code null}
     */
    ApsScenarioDO getScenarioDO(Long id);

    /**
     * 批量获得排程场景 DO
     *
     * @param ids 编号集合
     * @return 排程场景列表
     */
    List<ApsScenarioDO> getScenarioDOList(Collection<Long> ids);

}
