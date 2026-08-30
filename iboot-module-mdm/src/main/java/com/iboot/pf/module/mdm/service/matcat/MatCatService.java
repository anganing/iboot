package com.iboot.pf.module.mdm.service.matcat;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mdm.controller.admin.matcat.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.matcat.MatCatDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * 物料分类 Service 接口
 *
 * @author iboot
 */
public interface MatCatService {

    /**
     * 创建物料分类
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMatCat(@Valid MatCatSaveReqVO createReqVO);

    /**
     * 更新物料分类
     *
     * @param updateReqVO 更新信息
     */
    void updateMatCat(@Valid MatCatSaveReqVO updateReqVO);

    /**
     * 删除物料分类
     *
     * @param id 编号
     */
    void deleteMatCat(Long id);


    /**
     * 获得物料分类
     *
     * @param id 编号
     * @return 物料分类
     */
    MatCatRespVO getMatCat(Long id);

    /**
     * 获得物料分类列表
     *
     * @param listReqVO 查询条件
     * @return 物料分类列表
     */
    List<MatCatRespVO> getMatCatList(MatCatListReqVO listReqVO);

    /**
     * 获得全部物料分类（含停用），供 API 层构造分类树
     *
     * @return 物料分类列表
     */
    List<MatCatDO> getMatCatDOList();

}