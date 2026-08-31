package com.iboot.pf.module.mes.service.kitsnapshot;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mes.controller.admin.kitsnapshot.vo.KitSnapshotPageReqVO;
import com.iboot.pf.module.mes.controller.admin.kitsnapshot.vo.KitSnapshotRespVO;
import com.iboot.pf.module.mes.dal.mysql.kitsnapshot.KitSnapshotMapper;
import com.iboot.pf.module.mes.service.snapshot.MesRelationFiller;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;

/**
 * MES-齐套检查快照 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class KitSnapshotServiceImpl implements KitSnapshotService {

    @Resource
    private KitSnapshotMapper kitSnapshotMapper;
    @Resource
    private MesRelationFiller relationFiller;

    @Override
    public KitSnapshotRespVO getKitSnapshot(Long id) {
        KitSnapshotRespVO respVO = kitSnapshotMapper.selectJoinById(id);
        fillRelationNames(respVO == null ? Collections.emptyList() : Collections.singletonList(respVO));
        return respVO;
    }

    @Override
    public PageResult<KitSnapshotRespVO> getKitSnapshotPage(KitSnapshotPageReqVO pageReqVO) {
        PageResult<KitSnapshotRespVO> pageResult = kitSnapshotMapper.selectPage(pageReqVO);
        fillRelationNames(pageResult.getList());
        return pageResult;
    }

    /** 替代料在 mdm 模块，跨模块只能走门面批量补全。 */
    private void fillRelationNames(List<KitSnapshotRespVO> list) {
        relationFiller.fillMaterialName(list, KitSnapshotRespVO::getAltMatId, KitSnapshotRespVO::setAltMatName);
    }

}
