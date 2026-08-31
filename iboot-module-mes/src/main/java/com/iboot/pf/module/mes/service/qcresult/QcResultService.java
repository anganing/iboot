package com.iboot.pf.module.mes.service.qcresult;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mes.controller.admin.qcresult.vo.QcResultPageReqVO;
import com.iboot.pf.module.mes.controller.admin.qcresult.vo.QcResultRespVO;
import com.iboot.pf.module.mes.controller.admin.qcresult.vo.QcResultSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.qcresult.QcResultDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES-检验结果 Service 接口
 *
 * @author iboot
 */
public interface QcResultService {

    /**
     * 创建检验结果
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createQcResult(@Valid QcResultSaveReqVO createReqVO);

    /**
     * 更新检验结果
     *
     * @param updateReqVO 更新信息
     */
    void updateQcResult(@Valid QcResultSaveReqVO updateReqVO);

    /**
     * 删除检验结果
     *
     * @param id 编号
     */
    void deleteQcResult(Long id);

    /**
     * 批量删除检验结果
     *
     * @param ids 编号
     */
    void deleteQcResultListByIds(List<Long> ids);

    /**
     * 获得检验结果
     *
     * @param id 编号
     * @return 检验结果
     */
    QcResultRespVO getQcResult(Long id);

    /**
     * 获得检验结果分页
     *
     * @param pageReqVO 分页查询
     * @return 检验结果分页
     */
    PageResult<QcResultRespVO> getQcResultPage(QcResultPageReqVO pageReqVO);

    /**
     * 校验检验结果存在并返回，供缺陷记录引用时使用
     *
     * @param id 编号
     * @return 检验结果
     */
    QcResultDO validateQcResultExists(Long id);

}
