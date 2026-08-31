package com.iboot.pf.module.mes.service.qcitem;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mes.controller.admin.qcitem.vo.QcItemPageReqVO;
import com.iboot.pf.module.mes.controller.admin.qcitem.vo.QcItemRespVO;
import com.iboot.pf.module.mes.controller.admin.qcitem.vo.QcItemSaveReqVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES-检验项目 Service 接口
 *
 * @author iboot
 */
public interface QcItemService {

    /**
     * 创建检验项目
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createQcItem(@Valid QcItemSaveReqVO createReqVO);

    /**
     * 更新检验项目
     *
     * @param updateReqVO 更新信息
     */
    void updateQcItem(@Valid QcItemSaveReqVO updateReqVO);

    /**
     * 删除检验项目
     *
     * @param id 编号
     */
    void deleteQcItem(Long id);

    /**
     * 批量删除检验项目
     *
     * @param ids 编号
     */
    void deleteQcItemListByIds(List<Long> ids);

    /**
     * 获得检验项目
     *
     * @param id 编号
     * @return 检验项目
     */
    QcItemRespVO getQcItem(Long id);

    /**
     * 获得检验项目分页
     *
     * @param pageReqVO 分页查询
     * @return 检验项目分页
     */
    PageResult<QcItemRespVO> getQcItemPage(QcItemPageReqVO pageReqVO);

}
