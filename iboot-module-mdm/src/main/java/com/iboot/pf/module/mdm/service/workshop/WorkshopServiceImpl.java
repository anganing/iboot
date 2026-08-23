package com.iboot.pf.module.mdm.service.workshop;

import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import com.iboot.pf.module.mdm.controller.admin.workshop.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.workshop.WorkshopDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mdm.dal.mysql.workshop.WorkshopMapper;
import com.iboot.pf.module.mdm.dal.mysql.plant.PlantMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.*;

/**
 * 车间 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class WorkshopServiceImpl implements WorkshopService {

    @Resource
    private WorkshopMapper workshopMapper;
    @Resource
    private MdmCodeService mdmCodeService;
    @Resource
    private PlantMapper plantMapper;

    @Override
    public Long createWorkshop(WorkshopSaveReqVO createReqVO) {
        validatePlantExists(createReqVO.getPlantId());
        // 插入
        WorkshopDO workshop = BeanUtils.toBean(createReqVO, WorkshopDO.class);
        workshop.setId(null);
        workshop.setCode(mdmCodeService.resolveCode(workshop.getCode(), "MDM_WORKSHOP_CODE"));
        workshopMapper.insert(workshop);

        // 返回
        return workshop.getId();
    }

    @Override
    public void updateWorkshop(WorkshopSaveReqVO updateReqVO) {
        // 校验存在
        validateWorkshopExists(updateReqVO.getId());
        validatePlantExists(updateReqVO.getPlantId());
        // 更新
        WorkshopDO updateObj = BeanUtils.toBean(updateReqVO, WorkshopDO.class);
        workshopMapper.updateById(updateObj);
    }

    @Override
    public void deleteWorkshop(Long id) {
        // 校验存在
        validateWorkshopExists(id);
        // 删除
        workshopMapper.deleteById(id);
    }

    @Override
    public void deleteWorkshopListByIds(List<Long> ids) {
        // 删除
        workshopMapper.deleteByIds(ids);
    }


    private void validateWorkshopExists(Long id) {
        if (workshopMapper.selectById(id) == null) {
            throw exception(WORKSHOP_NOT_EXISTS);
        }
    }

    private void validatePlantExists(Long plantId) {
        if (plantMapper.selectById(plantId) == null) {
            throw exception(PLANT_NOT_EXISTS);
        }
    }

    @Override
    public WorkshopDO getWorkshop(Long id) {
        return workshopMapper.selectById(id);
    }

    @Override
    public PageResult<WorkshopDO> getWorkshopPage(WorkshopPageReqVO pageReqVO) {
        return workshopMapper.selectPage(pageReqVO);
    }

}
