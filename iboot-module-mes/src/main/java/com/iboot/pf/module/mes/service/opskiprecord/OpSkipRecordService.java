package com.iboot.pf.module.mes.service.opskiprecord;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mes.controller.admin.opskiprecord.vo.OpSkipRecordPageReqVO;
import com.iboot.pf.module.mes.controller.admin.opskiprecord.vo.OpSkipRecordRespVO;
import com.iboot.pf.module.mes.controller.admin.opskiprecord.vo.OpSkipRecordSaveReqVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES-工序跳过记录 Service 接口
 *
 * @author iboot
 */
public interface OpSkipRecordService {

    /**
     * 创建工序跳过记录
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOpSkipRecord(@Valid OpSkipRecordSaveReqVO createReqVO);

    /**
     * 更新工序跳过记录
     *
     * @param updateReqVO 更新信息
     */
    void updateOpSkipRecord(@Valid OpSkipRecordSaveReqVO updateReqVO);

    /**
     * 删除工序跳过记录
     *
     * @param id 编号
     */
    void deleteOpSkipRecord(Long id);

    /**
     * 批量删除工序跳过记录
     *
     * @param ids 编号
     */
    void deleteOpSkipRecordListByIds(List<Long> ids);

    /**
     * 获得工序跳过记录
     *
     * @param id 编号
     * @return 工序跳过记录
     */
    OpSkipRecordRespVO getOpSkipRecord(Long id);

    /**
     * 获得工序跳过记录分页
     *
     * @param pageReqVO 分页查询
     * @return 工序跳过记录分页
     */
    PageResult<OpSkipRecordRespVO> getOpSkipRecordPage(OpSkipRecordPageReqVO pageReqVO);

}
