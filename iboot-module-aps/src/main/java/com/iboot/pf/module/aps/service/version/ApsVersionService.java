package com.iboot.pf.module.aps.service.version;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.aps.api.version.dto.ChangeVersionStatusReqDTO;
import com.iboot.pf.module.aps.api.version.dto.GenerateVersionReqDTO;
import com.iboot.pf.module.aps.api.version.dto.ReplaceVersionReqDTO;
import com.iboot.pf.module.aps.controller.admin.version.vo.VersionPageReqVO;
import com.iboot.pf.module.aps.controller.admin.version.vo.VersionRespVO;
import com.iboot.pf.module.aps.dal.dataobject.version.ApsVersionDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * APS-排程版本 Service 接口。
 *
 * <p>版本是求解产出的结果表：只提供查询、导出与状态流转动作，不提供人工新增、修改、删除。
 * 放开人工写入等于允许伪造排程结果与发布记录。
 *
 * @author iboot
 */
public interface ApsVersionService {

    /**
     * 获得排程版本
     *
     * @param id 编号
     * @return 排程版本
     */
    VersionRespVO getVersion(Long id);

    /**
     * 获得排程版本分页
     *
     * @param pageReqVO 分页查询
     * @return 排程版本分页
     */
    PageResult<VersionRespVO> getVersionPage(VersionPageReqVO pageReqVO);

    /**
     * 生成排程版本
     *
     * @param reqDTO 命令
     * @return 版本编号
     */
    Long generateVersion(@Valid GenerateVersionReqDTO reqDTO);

    /**
     * 确认排程版本
     *
     * @param reqDTO 命令
     */
    void confirmVersion(@Valid ChangeVersionStatusReqDTO reqDTO);

    /**
     * 发布排程版本，同场景下原已发布版本自动置为已被替换
     *
     * @param reqDTO 命令
     */
    void publishVersion(@Valid ChangeVersionStatusReqDTO reqDTO);

    /**
     * 用新版本替换已发布版本
     *
     * @param reqDTO 命令
     */
    void replaceVersion(@Valid ReplaceVersionReqDTO reqDTO);

    /**
     * 校验版本的排程结果可修改，已发布或已被替换时抛异常
     *
     * @param versionId 版本编号
     */
    void validateMutable(Long versionId);

    /**
     * 获得排程版本 DO，供模块内与 Api 门面使用
     *
     * @param id 编号
     * @return 排程版本；不存在返回 {@code null}
     */
    ApsVersionDO getVersionDO(Long id);

    /**
     * 按场景获得排程版本列表，最新版本在前
     *
     * @param scenarioId 场景编号
     * @return 版本列表
     */
    List<ApsVersionDO> getVersionDOListByScenarioId(Long scenarioId);

    /**
     * 求解成功后回填得分与结束时间，状态推进到已求解
     *
     * @param versionId 版本编号
     * @param score 综合得分的可读表示
     * @param hardScore 硬约束得分
     * @param softScore 软约束得分
     * @param solverMessage 求解结果说明
     */
    void markSolved(Long versionId, String score, Long hardScore, Long softScore, String solverMessage);

    /**
     * 求解失败或无可行解时置为求解失败并记录原因
     *
     * @param versionId 版本编号
     * @param solverMessage 失败原因
     */
    void markFailed(Long versionId, String solverMessage);

}
