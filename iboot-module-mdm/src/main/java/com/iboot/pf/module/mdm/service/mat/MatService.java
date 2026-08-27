package com.iboot.pf.module.mdm.service.mat;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mdm.controller.admin.mat.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.mat.MatDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * 物料主档 Service 接口
 *
 * @author iboot
 */
public interface MatService {

    /**
     * 创建物料主档
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMat(@Valid MatSaveReqVO createReqVO);

    /**
     * 更新物料主档
     *
     * @param updateReqVO 更新信息
     */
    void updateMat(@Valid MatSaveReqVO updateReqVO);

    /**
     * 删除物料主档
     *
     * @param id 编号
     */
    void deleteMat(Long id);

    /**
    * 批量删除物料主档
    *
    * @param ids 编号
    */
    void deleteMatListByIds(List<Long> ids);

    /**
     * 获得物料主档
     *
     * @param id 编号
     * @return 物料主档
     */
    MatRespVO getMat(Long id);

    /**
     * 获得物料主档分页
     *
     * @param pageReqVO 分页查询
     * @return 物料主档分页
     */
    PageResult<MatRespVO> getMatPage(MatPageReqVO pageReqVO);

}