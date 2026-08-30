package com.iboot.pf.module.aps.service.version;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.aps.controller.admin.version.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.version.VersionDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * APS-排程版本 Service 接口
 *
 * @author iboot
 */
public interface VersionService {

    /**
     * 创建APS-排程版本
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createVersion(@Valid VersionSaveReqVO createReqVO);

    /**
     * 导入APS-排程版本
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    VersionImportRespVO importVersionList(List<VersionImportExcelVO> importList);

    /**
     * 更新APS-排程版本
     *
     * @param updateReqVO 更新信息
     */
    void updateVersion(@Valid VersionSaveReqVO updateReqVO);

    /**
     * 删除APS-排程版本
     *
     * @param id 编号
     */
    void deleteVersion(Long id);

    /**
    * 批量删除APS-排程版本
    *
    * @param ids 编号
    */
    void deleteVersionListByIds(List<Long> ids);

    /**
     * 获得APS-排程版本
     *
     * @param id 编号
     * @return APS-排程版本
     */
    VersionRespVO getVersion(Long id);

    /**
     * 获得APS-排程版本分页
     *
     * @param pageReqVO 分页查询
     * @return APS-排程版本分页
     */
    PageResult<VersionRespVO> getVersionPage(VersionPageReqVO pageReqVO);

}