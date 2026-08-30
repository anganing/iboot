package com.iboot.pf.module.mes.service.opskiprecord;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mes.controller.admin.opskiprecord.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.opskiprecord.OpSkipRecordDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * MES-工序跳过记录 Service 接口
 *
 * @author iboot
 */
public interface OpSkipRecordService {

    /**
     * 创建MES-工序跳过记录
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOpSkipRecord(@Valid OpSkipRecordSaveReqVO createReqVO);

    /**
     * 导入MES-工序跳过记录
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    OpSkipRecordImportRespVO importOpSkipRecordList(List<OpSkipRecordImportExcelVO> importList);

    /**
     * 更新MES-工序跳过记录
     *
     * @param updateReqVO 更新信息
     */
    void updateOpSkipRecord(@Valid OpSkipRecordSaveReqVO updateReqVO);

    /**
     * 删除MES-工序跳过记录
     *
     * @param id 编号
     */
    void deleteOpSkipRecord(Long id);

    /**
    * 批量删除MES-工序跳过记录
    *
    * @param ids 编号
    */
    void deleteOpSkipRecordListByIds(List<Long> ids);

    /**
     * 获得MES-工序跳过记录
     *
     * @param id 编号
     * @return MES-工序跳过记录
     */
    OpSkipRecordRespVO getOpSkipRecord(Long id);

    /**
     * 获得MES-工序跳过记录分页
     *
     * @param pageReqVO 分页查询
     * @return MES-工序跳过记录分页
     */
    PageResult<OpSkipRecordRespVO> getOpSkipRecordPage(OpSkipRecordPageReqVO pageReqVO);

}