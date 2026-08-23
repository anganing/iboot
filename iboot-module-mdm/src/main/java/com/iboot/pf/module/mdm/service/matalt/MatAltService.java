package com.iboot.pf.module.mdm.service.matalt;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mdm.controller.admin.matalt.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.matalt.MatAltDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * 替代料 Service 接口
 *
 * @author iboot
 */
public interface MatAltService {

    /**
     * 创建替代料
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMatAlt(@Valid MatAltSaveReqVO createReqVO);

    /**
     * 更新替代料
     *
     * @param updateReqVO 更新信息
     */
    void updateMatAlt(@Valid MatAltSaveReqVO updateReqVO);

    /**
     * 删除替代料
     *
     * @param id 编号
     */
    void deleteMatAlt(Long id);

    /**
    * 批量删除替代料
    *
    * @param ids 编号
    */
    void deleteMatAltListByIds(List<Long> ids);

    /**
     * 获得替代料
     *
     * @param id 编号
     * @return 替代料
     */
    MatAltDO getMatAlt(Long id);

    /**
     * 获得替代料分页
     *
     * @param pageReqVO 分页查询
     * @return 替代料分页
     */
    PageResult<MatAltDO> getMatAltPage(MatAltPageReqVO pageReqVO);

}