package com.iboot.pf.module.mdm.service.plant;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.controller.admin.plant.vo.PlantPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.plant.vo.PlantRespVO;
import com.iboot.pf.module.mdm.controller.admin.plant.vo.PlantSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.plant.PlantDO;
import com.iboot.pf.module.mdm.dal.mysql.invorg.InvOrgMapper;
import com.iboot.pf.module.mdm.dal.mysql.plant.PlantMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.INV_ORG_NOT_EXISTS;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.PLANT_NOT_EXISTS;
import static com.iboot.pf.module.mdm.enums.MdmCodeRuleConstants.PLANT;

/**
 * 工厂 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class PlantServiceImpl implements PlantService {

    @Resource
    private PlantMapper plantMapper;
    @Resource
    private MdmCodeService mdmCodeService;
    @Resource
    private InvOrgMapper invOrgMapper;

    @Override
    public Long createPlant(PlantSaveReqVO createReqVO) {
        validateInvOrgExists(createReqVO.getInvOrgId());
        // 插入
        PlantDO plant = BeanUtils.toBean(createReqVO, PlantDO.class);
        plant.setId(null);
        plant.setCode(mdmCodeService.resolveCode(plant.getCode(), PLANT));
        plantMapper.insert(plant);

        // 返回
        return plant.getId();
    }

    @Override
    public void updatePlant(PlantSaveReqVO updateReqVO) {
        // 校验存在
        validatePlantExists(updateReqVO.getId());
        validateInvOrgExists(updateReqVO.getInvOrgId());
        // 更新
        PlantDO updateObj = BeanUtils.toBean(updateReqVO, PlantDO.class);
        plantMapper.updateById(updateObj);
    }

    @Override
    public void deletePlant(Long id) {
        // 校验存在
        validatePlantExists(id);
        // 删除
        plantMapper.deleteById(id);
    }

    @Override
        public void deletePlantListByIds(List<Long> ids) {
        // 删除
        plantMapper.deleteByIds(ids);
        }


    private void validatePlantExists(Long id) {
        if (plantMapper.selectById(id) == null) {
            throw exception(PLANT_NOT_EXISTS);
        }
    }

    private void validateInvOrgExists(Long invOrgId) {
        if (invOrgMapper.selectById(invOrgId) == null) {
            throw exception(INV_ORG_NOT_EXISTS);
        }
    }

    @Override
    public PlantRespVO getPlant(Long id) {
        return plantMapper.selectJoinById(id);
    }

    @Override
    public PageResult<PlantRespVO> getPlantPage(PlantPageReqVO pageReqVO) {
        return plantMapper.selectPage(pageReqVO);
    }

}
