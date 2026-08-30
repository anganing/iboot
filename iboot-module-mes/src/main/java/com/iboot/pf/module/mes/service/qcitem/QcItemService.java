package com.iboot.pf.module.mes.service.qcitem;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mes.controller.admin.qcitem.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.qcitem.QcItemDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * MES-检验项目 Service 接口
 *
 * @author iboot
 */
public interface QcItemService {

    /**
     * 创建MES-检验项目
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createQcItem(@Valid QcItemSaveReqVO createReqVO);

    /**
     * 导入MES-检验项目
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    QcItemImportRespVO importQcItemList(List<QcItemImportExcelVO> importList);

    /**
     * 更新MES-检验项目
     *
     * @param updateReqVO 更新信息
     */
    void updateQcItem(@Valid QcItemSaveReqVO updateReqVO);

    /**
     * 删除MES-检验项目
     *
     * @param id 编号
     */
    void deleteQcItem(Long id);

    /**
    * 批量删除MES-检验项目
    *
    * @param ids 编号
    */
    void deleteQcItemListByIds(List<Long> ids);

    /**
     * 获得MES-检验项目
     *
     * @param id 编号
     * @return MES-检验项目
     */
    QcItemRespVO getQcItem(Long id);

    /**
     * 获得MES-检验项目分页
     *
     * @param pageReqVO 分页查询
     * @return MES-检验项目分页
     */
    PageResult<QcItemRespVO> getQcItemPage(QcItemPageReqVO pageReqVO);

}