package com.iboot.pf.module.aps.service.projectionmat;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.aps.controller.admin.projectionmat.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.projectionmat.ProjectionMatDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * 物料投影 Service 接口
 *
 * @author iboot
 */
public interface ProjectionMatService {

    /**
     * 创建物料投影
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProjectionMat(@Valid ProjectionMatSaveReqVO createReqVO);

    /**
     * 导入物料投影
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    ProjectionMatImportRespVO importProjectionMatList(List<ProjectionMatImportExcelVO> importList);

    /**
     * 更新物料投影
     *
     * @param updateReqVO 更新信息
     */
    void updateProjectionMat(@Valid ProjectionMatSaveReqVO updateReqVO);

    /**
     * 删除物料投影
     *
     * @param id 编号
     */
    void deleteProjectionMat(Long id);

    /**
    * 批量删除物料投影
    *
    * @param ids 编号
    */
    void deleteProjectionMatListByIds(List<Long> ids);

    /**
     * 获得物料投影
     *
     * @param id 编号
     * @return 物料投影
     */
    ProjectionMatRespVO getProjectionMat(Long id);

    /**
     * 获得物料投影分页
     *
     * @param pageReqVO 分页查询
     * @return 物料投影分页
     */
    PageResult<ProjectionMatRespVO> getProjectionMatPage(ProjectionMatPageReqVO pageReqVO);

}