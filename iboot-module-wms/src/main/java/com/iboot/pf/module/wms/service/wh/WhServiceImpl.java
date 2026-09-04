package com.iboot.pf.module.wms.service.wh;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.wms.controller.admin.wh.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsWhDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mdm.api.org.OrgApi;
import com.iboot.pf.module.mdm.api.org.dto.InvOrgRespDTO;
import com.iboot.pf.module.mdm.api.org.dto.PlantRespDTO;
import com.iboot.pf.module.wms.dal.mysql.wh.WhMapper;
import com.iboot.pf.module.wms.dal.mysql.zone.ZoneMapper;
import com.iboot.pf.module.wms.dal.mysql.loc.LocMapper;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsZoneDO;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsLocDO;
import com.iboot.pf.module.wms.enums.WmsCodeRuleConstants;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertSet;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.framework.common.util.collection.MapUtils.findAndThen;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS-仓库 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class WhServiceImpl implements WhService {

    @Resource
    private WhMapper whMapper;
    @Resource
    private ZoneMapper zoneMapper;
    @Resource
    private LocMapper locMapper;
    @Resource
    private WmsCodeService codeService;
    @Resource
    private OrgApi orgApi;

    @Override
    public Long createWh(WhSaveReqVO createReqVO) {
        // 校验组织与工厂存在、编码在同组织内唯一
        validateInvOrgAndPlant(createReqVO.getInvOrgId(), createReqVO.getPlantId());
        // 插入
        WmsWhDO wh = BeanUtils.toBean(createReqVO, WmsWhDO.class);
        wh.setId(null);
        wh.setCode(codeService.generate(WmsCodeRuleConstants.WH, wh.getCode()));
        validateCodeUnique(wh.getInvOrgId(), wh.getCode(), null);
        whMapper.insert(wh);

        // 返回
        return wh.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public WhImportRespVO importWhList(List<WhImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return WhImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        WhImportRespVO respVO = WhImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createWh(BeanUtils.toBean(importItem, WhSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateWh(WhSaveReqVO updateReqVO) {
        // 校验存在
        validateWhExists(updateReqVO.getId());
        validateInvOrgAndPlant(updateReqVO.getInvOrgId(), updateReqVO.getPlantId());
        // 改自己不能撞自己，所以排除自身编号
        validateCodeUnique(updateReqVO.getInvOrgId(), updateReqVO.getCode(), updateReqVO.getId());
        // 更新
        WmsWhDO updateObj = BeanUtils.toBean(updateReqVO, WmsWhDO.class);
        whMapper.updateById(updateObj);
    }

    @Override
    public void deleteWh(Long id) {
        // 校验存在
        validateWhExists(id);
        // 仓库下还有库区或库位时禁止删除，避免留下孤儿数据
        validateWhUnused(id);
        // 删除
        whMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWhListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        ids.forEach(this::validateWhUnused);
        whMapper.deleteByIds(ids);
    }


    private void validateWhExists(Long id) {
        if (whMapper.selectById(id) == null) {
            throw exception(WH_NOT_EXISTS);
        }
    }

    /**
     * 编码唯一性：唯一索引是 (inv_org_id, code, deleted)，这里做前置校验给出可读提示。
     */
    private void validateCodeUnique(Long invOrgId, String code, Long excludeId) {
        WmsWhDO existed = whMapper.selectByInvOrgIdAndCode(invOrgId, code);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(WH_CODE_DUPLICATE);
    }

    /**
     * 跨模块取库存组织与工厂只能走 mdm 的 OrgApi 门面，不允许 JOIN mdm 的表。
     */
    private void validateInvOrgAndPlant(Long invOrgId, Long plantId) {
        if (orgApi.getInvOrgMap(Collections.singletonList(invOrgId)).get(invOrgId) == null) {
            throw exception(WH_INV_ORG_NOT_EXISTS);
        }
        if (orgApi.getPlantMap(Collections.singletonList(plantId)).get(plantId) == null) {
            throw exception(WH_PLANT_NOT_EXISTS);
        }
    }

    private void validateWhUnused(Long id) {
        if (zoneMapper.selectCount(WmsZoneDO::getWhId, id) > 0) {
            throw exception(WH_HAS_ZONE);
        }
        if (locMapper.selectCount(WmsLocDO::getWhId, id) > 0) {
            throw exception(WH_HAS_LOC);
        }
    }

    @Override
    public WhRespVO getWh(Long id) {
        WhRespVO wh = whMapper.selectJoinById(id);
        if (wh != null) {
            fillOrgName(Collections.singletonList(wh));
        }
        return wh;
    }

    @Override
    public PageResult<WhRespVO> getWhPage(WhPageReqVO pageReqVO) {
        PageResult<WhRespVO> pageResult = whMapper.selectPage(pageReqVO);
        fillOrgName(pageResult.getList());
        return pageResult;
    }

    /**
     * 批量补全库存组织与工厂名称。放在 Service 而不是 Controller，
     * Excel 导出复用同一个分页结果才不会丢字段。
     */
    private void fillOrgName(List<WhRespVO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Map<Long, InvOrgRespDTO> invOrgMap = orgApi.getInvOrgMap(convertSet(list, WhRespVO::getInvOrgId));
        Map<Long, PlantRespDTO> plantMap = orgApi.getPlantMap(convertSet(list, WhRespVO::getPlantId));
        list.forEach(vo -> {
            findAndThen(invOrgMap, vo.getInvOrgId(), invOrg -> {
                vo.setInvOrgCode(invOrg.getCode());
                vo.setInvOrgName(invOrg.getName());
            });
            findAndThen(plantMap, vo.getPlantId(), plant -> {
                vo.setPlantCode(plant.getCode());
                vo.setPlantName(plant.getName());
            });
        });
    }

}
