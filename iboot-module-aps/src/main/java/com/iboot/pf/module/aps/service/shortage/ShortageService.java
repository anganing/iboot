package com.iboot.pf.module.aps.service.shortage;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.aps.controller.admin.shortage.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.shortage.ShortageDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * APS-排程缺料清单 Service 接口
 *
 * @author iboot
 */
public interface ShortageService {

    /**
     * 创建APS-排程缺料清单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createShortage(@Valid ShortageSaveReqVO createReqVO);

    /**
     * 导入APS-排程缺料清单
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    ShortageImportRespVO importShortageList(List<ShortageImportExcelVO> importList);

    /**
     * 更新APS-排程缺料清单
     *
     * @param updateReqVO 更新信息
     */
    void updateShortage(@Valid ShortageSaveReqVO updateReqVO);

    /**
     * 删除APS-排程缺料清单
     *
     * @param id 编号
     */
    void deleteShortage(Long id);

    /**
    * 批量删除APS-排程缺料清单
    *
    * @param ids 编号
    */
    void deleteShortageListByIds(List<Long> ids);

    /**
     * 获得APS-排程缺料清单
     *
     * @param id 编号
     * @return APS-排程缺料清单
     */
    ShortageRespVO getShortage(Long id);

    /**
     * 获得APS-排程缺料清单分页
     *
     * @param pageReqVO 分页查询
     * @return APS-排程缺料清单分页
     */
    PageResult<ShortageRespVO> getShortagePage(ShortagePageReqVO pageReqVO);

}