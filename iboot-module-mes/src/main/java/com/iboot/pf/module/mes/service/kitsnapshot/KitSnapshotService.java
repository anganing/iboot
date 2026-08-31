package com.iboot.pf.module.mes.service.kitsnapshot;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mes.controller.admin.kitsnapshot.vo.KitSnapshotPageReqVO;
import com.iboot.pf.module.mes.controller.admin.kitsnapshot.vo.KitSnapshotRespVO;

/**
 * MES-齐套检查快照 Service 接口
 *
 * <p>账表只读：快照是齐套检查那一刻的库存口径证据，人工改动会让齐套判断失去意义，
 * 所以这里只有查询方法。
 *
 * @author iboot
 */
public interface KitSnapshotService {

    /**
     * 获得齐套检查快照
     *
     * @param id 编号
     * @return 齐套检查快照
     */
    KitSnapshotRespVO getKitSnapshot(Long id);

    /**
     * 获得齐套检查快照分页
     *
     * @param pageReqVO 分页查询
     * @return 齐套检查快照分页
     */
    PageResult<KitSnapshotRespVO> getKitSnapshotPage(KitSnapshotPageReqVO pageReqVO);

}
