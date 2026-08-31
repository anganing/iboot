package com.iboot.pf.module.aps.service.shortage;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.aps.api.shortage.dto.RefreshShortageReqDTO;
import com.iboot.pf.module.aps.controller.admin.shortage.vo.ShortageHandleReqVO;
import com.iboot.pf.module.aps.controller.admin.shortage.vo.ShortagePageReqVO;
import com.iboot.pf.module.aps.controller.admin.shortage.vo.ShortageRespVO;
import com.iboot.pf.module.aps.dal.dataobject.shortage.ApsShortageDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * APS-排程缺料清单 Service 接口。
 *
 * <p>缺料由“刷新缺料”按库存投影整体重算：管理后台只提供查询、导出、补充预计供给时间与忽略，
 * 不提供新增与删除。人工改数量会让缺料表和库存投影对不上。
 *
 * @author iboot
 */
public interface ApsShortageService {

    /**
     * 获得缺料
     *
     * @param id 编号
     * @return 缺料
     */
    ShortageRespVO getShortage(Long id);

    /**
     * 获得缺料分页
     *
     * @param pageReqVO 分页查询
     * @return 缺料分页
     */
    PageResult<ShortageRespVO> getShortagePage(ShortagePageReqVO pageReqVO);

    /**
     * 人工补充预计供给时间与备注
     *
     * @param handleReqVO 处理信息
     */
    void handleShortage(@Valid ShortageHandleReqVO handleReqVO);

    /**
     * 忽略缺料，后续排程不再提示
     *
     * @param id 编号
     */
    void ignoreShortage(Long id);

    /**
     * 按库存投影重算缺料的可用数量、短缺数量与状态
     *
     * @param reqDTO 命令
     */
    void refreshShortage(@Valid RefreshShortageReqDTO reqDTO);

    /**
     * 按版本获得缺料列表
     *
     * @param versionId 版本编号
     * @return 缺料列表
     */
    List<ApsShortageDO> getShortageDOListByVersionId(Long versionId);

}
