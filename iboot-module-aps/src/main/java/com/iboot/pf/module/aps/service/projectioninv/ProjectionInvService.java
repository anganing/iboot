package com.iboot.pf.module.aps.service.projectioninv;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.aps.controller.admin.projectioninv.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.projectioninv.ProjectionInvDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * 库存投影 Service 接口
 *
 * @author iboot
 */
public interface ProjectionInvService {

    /**
     * 创建库存投影
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProjectionInv(@Valid ProjectionInvSaveReqVO createReqVO);

    /**
     * 导入库存投影
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    ProjectionInvImportRespVO importProjectionInvList(List<ProjectionInvImportExcelVO> importList);

    /**
     * 更新库存投影
     *
     * @param updateReqVO 更新信息
     */
    void updateProjectionInv(@Valid ProjectionInvSaveReqVO updateReqVO);

    /**
     * 删除库存投影
     *
     * @param id 编号
     */
    void deleteProjectionInv(Long id);

    /**
    * 批量删除库存投影
    *
    * @param ids 编号
    */
    void deleteProjectionInvListByIds(List<Long> ids);

    /**
     * 获得库存投影
     *
     * @param id 编号
     * @return 库存投影
     */
    ProjectionInvRespVO getProjectionInv(Long id);

    /**
     * 获得库存投影分页
     *
     * @param pageReqVO 分页查询
     * @return 库存投影分页
     */
    PageResult<ProjectionInvRespVO> getProjectionInvPage(ProjectionInvPageReqVO pageReqVO);

}