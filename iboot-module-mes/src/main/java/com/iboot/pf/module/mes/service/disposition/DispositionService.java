package com.iboot.pf.module.mes.service.disposition;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mes.controller.admin.disposition.vo.DispositionPageReqVO;
import com.iboot.pf.module.mes.controller.admin.disposition.vo.DispositionRespVO;
import com.iboot.pf.module.mes.controller.admin.disposition.vo.DispositionSaveReqVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES-不合格品处置单 Service 接口
 *
 * @author iboot
 */
public interface DispositionService {

    /**
     * 创建处置单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDisposition(@Valid DispositionSaveReqVO createReqVO);

    /**
     * 更新处置单
     *
     * @param updateReqVO 更新信息
     */
    void updateDisposition(@Valid DispositionSaveReqVO updateReqVO);

    /**
     * 删除处置单
     *
     * @param id 编号
     */
    void deleteDisposition(Long id);

    /**
     * 批量删除处置单
     *
     * @param ids 编号
     */
    void deleteDispositionListByIds(List<Long> ids);

    /**
     * 获得处置单
     *
     * @param id 编号
     * @return 处置单
     */
    DispositionRespVO getDisposition(Long id);

    /**
     * 获得处置单分页
     *
     * @param pageReqVO 分页查询
     * @return 处置单分页
     */
    PageResult<DispositionRespVO> getDispositionPage(DispositionPageReqVO pageReqVO);

}
