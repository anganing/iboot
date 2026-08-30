package com.iboot.pf.module.aps.service.load;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.aps.controller.admin.load.vo.*;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 资源负荷 Service 接口
 *
 * @author iboot
 */
public interface ApsLoadService {

    /**
     * 创建资源负荷
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createLoad(@Valid LoadSaveReqVO createReqVO);

    /**
     * 导入资源负荷
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    LoadImportRespVO importLoadList(List<LoadImportExcelVO> importList);

    /**
     * 更新资源负荷
     *
     * @param updateReqVO 更新信息
     */
    void updateLoad(@Valid LoadSaveReqVO updateReqVO);

    /**
     * 删除资源负荷
     *
     * @param id 编号
     */
    void deleteLoad(Long id);

    /**
    * 批量删除资源负荷
    *
    * @param ids 编号
    */
    void deleteLoadListByIds(List<Long> ids);

    /**
     * 获得资源负荷
     *
     * @param id 编号
     * @return 资源负荷
     */
    LoadRespVO getLoad(Long id);

    /**
     * 获得资源负荷分页
     *
     * @param pageReqVO 分页查询
     * @return 资源负荷分页
     */
    PageResult<LoadRespVO> getLoadPage(LoadPageReqVO pageReqVO);

    /**
     * 根据版本ID获得资源负荷列表
     *
     * @param versionId 版本ID
     * @return 资源负荷列表
     */
    List<LoadRespVO> getLoadListByVersionId(Long versionId);

    /**
     * 刷新资源负荷
     *
     * @param r 刷新请求
     */
    void refreshLoad(com.iboot.pf.module.aps.api.load.dto.RefreshLoadReqDTO r);

}
