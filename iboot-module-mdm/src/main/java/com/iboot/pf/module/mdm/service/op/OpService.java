package com.iboot.pf.module.mdm.service.op;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mdm.controller.admin.op.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.op.OpDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * 标准工序 Service 接口
 *
 * @author iboot
 */
public interface OpService {

    /**
     * 创建标准工序
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOp(@Valid OpSaveReqVO createReqVO);

    /**
     * 更新标准工序
     *
     * @param updateReqVO 更新信息
     */
    void updateOp(@Valid OpSaveReqVO updateReqVO);

    /**
     * 删除标准工序
     *
     * @param id 编号
     */
    void deleteOp(Long id);

    /**
    * 批量删除标准工序
    *
    * @param ids 编号
    */
    void deleteOpListByIds(List<Long> ids);

    /**
     * 获得标准工序
     *
     * @param id 编号
     * @return 标准工序
     */
    OpDO getOp(Long id);

    /**
     * 获得标准工序分页
     *
     * @param pageReqVO 分页查询
     * @return 标准工序分页
     */
    PageResult<OpDO> getOpPage(OpPageReqVO pageReqVO);

}