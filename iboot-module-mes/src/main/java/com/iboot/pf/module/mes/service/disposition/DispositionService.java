package com.iboot.pf.module.mes.service.disposition;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mes.controller.admin.disposition.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.disposition.DispositionDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * MES-不合格品处置单 Service 接口
 *
 * @author iboot
 */
public interface DispositionService {

    /**
     * 创建MES-不合格品处置单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDisposition(@Valid DispositionSaveReqVO createReqVO);

    /**
     * 导入MES-不合格品处置单
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    DispositionImportRespVO importDispositionList(List<DispositionImportExcelVO> importList);

    /**
     * 更新MES-不合格品处置单
     *
     * @param updateReqVO 更新信息
     */
    void updateDisposition(@Valid DispositionSaveReqVO updateReqVO);

    /**
     * 删除MES-不合格品处置单
     *
     * @param id 编号
     */
    void deleteDisposition(Long id);

    /**
    * 批量删除MES-不合格品处置单
    *
    * @param ids 编号
    */
    void deleteDispositionListByIds(List<Long> ids);

    /**
     * 获得MES-不合格品处置单
     *
     * @param id 编号
     * @return MES-不合格品处置单
     */
    DispositionRespVO getDisposition(Long id);

    /**
     * 获得MES-不合格品处置单分页
     *
     * @param pageReqVO 分页查询
     * @return MES-不合格品处置单分页
     */
    PageResult<DispositionRespVO> getDispositionPage(DispositionPageReqVO pageReqVO);

}