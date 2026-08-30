package com.iboot.pf.module.mes.service.kitsnapshot;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mes.controller.admin.kitsnapshot.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.kitsnapshot.KitSnapshotDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * MES-齐套检查快照 Service 接口
 *
 * @author iboot
 */
public interface KitSnapshotService {

    /**
     * 创建MES-齐套检查快照
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createKitSnapshot(@Valid KitSnapshotSaveReqVO createReqVO);

    /**
     * 导入MES-齐套检查快照
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    KitSnapshotImportRespVO importKitSnapshotList(List<KitSnapshotImportExcelVO> importList);

    /**
     * 更新MES-齐套检查快照
     *
     * @param updateReqVO 更新信息
     */
    void updateKitSnapshot(@Valid KitSnapshotSaveReqVO updateReqVO);

    /**
     * 删除MES-齐套检查快照
     *
     * @param id 编号
     */
    void deleteKitSnapshot(Long id);

    /**
    * 批量删除MES-齐套检查快照
    *
    * @param ids 编号
    */
    void deleteKitSnapshotListByIds(List<Long> ids);

    /**
     * 获得MES-齐套检查快照
     *
     * @param id 编号
     * @return MES-齐套检查快照
     */
    KitSnapshotRespVO getKitSnapshot(Long id);

    /**
     * 获得MES-齐套检查快照分页
     *
     * @param pageReqVO 分页查询
     * @return MES-齐套检查快照分页
     */
    PageResult<KitSnapshotRespVO> getKitSnapshotPage(KitSnapshotPageReqVO pageReqVO);

}