package com.iboot.pf.module.mdm.service.shift;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.controller.admin.shift.vo.ShiftPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.shift.vo.ShiftSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.shift.ShiftDO;
import com.iboot.pf.module.mdm.dal.mysql.shift.ShiftMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.SHIFT_NOT_EXISTS;
import static com.iboot.pf.module.mdm.enums.MdmCodeRuleConstants.SHIFT;

/**
 * 班次 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ShiftServiceImpl implements ShiftService {

    @Resource
    private ShiftMapper shiftMapper;
    @Resource
    private MdmCodeService mdmCodeService;

    @Override
    public Long createShift(ShiftSaveReqVO createReqVO) {
        // 插入
        ShiftDO shift = BeanUtils.toBean(createReqVO, ShiftDO.class);
        shift.setId(null);
        shift.setCode(mdmCodeService.resolveCode(shift.getCode(), SHIFT));
        shiftMapper.insert(shift);

        // 返回
        return shift.getId();
    }

    @Override
    public void updateShift(ShiftSaveReqVO updateReqVO) {
        // 校验存在
        validateShiftExists(updateReqVO.getId());
        // 更新
        ShiftDO updateObj = BeanUtils.toBean(updateReqVO, ShiftDO.class);
        shiftMapper.updateById(updateObj);
    }

    @Override
    public void deleteShift(Long id) {
        // 校验存在
        validateShiftExists(id);
        // 删除
        shiftMapper.deleteById(id);
    }

    @Override
        public void deleteShiftListByIds(List<Long> ids) {
        // 删除
        shiftMapper.deleteByIds(ids);
        }


    private void validateShiftExists(Long id) {
        if (shiftMapper.selectById(id) == null) {
            throw exception(SHIFT_NOT_EXISTS);
        }
    }

    @Override
    public ShiftDO getShift(Long id) {
        return shiftMapper.selectById(id);
    }

    @Override
    public PageResult<ShiftDO> getShiftPage(ShiftPageReqVO pageReqVO) {
        return shiftMapper.selectPage(pageReqVO);
    }

}
