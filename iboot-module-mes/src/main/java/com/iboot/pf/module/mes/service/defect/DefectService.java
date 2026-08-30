package com.iboot.pf.module.mes.service.defect;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mes.controller.admin.defect.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.defect.DefectDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * MES-缺陷记录 Service 接口
 *
 * @author iboot
 */
public interface DefectService {

    /**
     * 创建MES-缺陷记录
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDefect(@Valid DefectSaveReqVO createReqVO);

    /**
     * 导入MES-缺陷记录
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    DefectImportRespVO importDefectList(List<DefectImportExcelVO> importList);

    /**
     * 更新MES-缺陷记录
     *
     * @param updateReqVO 更新信息
     */
    void updateDefect(@Valid DefectSaveReqVO updateReqVO);

    /**
     * 删除MES-缺陷记录
     *
     * @param id 编号
     */
    void deleteDefect(Long id);

    /**
    * 批量删除MES-缺陷记录
    *
    * @param ids 编号
    */
    void deleteDefectListByIds(List<Long> ids);

    /**
     * 获得MES-缺陷记录
     *
     * @param id 编号
     * @return MES-缺陷记录
     */
    DefectRespVO getDefect(Long id);

    /**
     * 获得MES-缺陷记录分页
     *
     * @param pageReqVO 分页查询
     * @return MES-缺陷记录分页
     */
    PageResult<DefectRespVO> getDefectPage(DefectPageReqVO pageReqVO);

}