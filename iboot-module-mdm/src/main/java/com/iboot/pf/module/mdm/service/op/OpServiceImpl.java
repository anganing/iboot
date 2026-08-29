package com.iboot.pf.module.mdm.service.op;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.controller.admin.op.vo.OpPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.op.vo.OpSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.op.OpDO;
import com.iboot.pf.module.mdm.dal.mysql.op.OpMapper;
import com.iboot.pf.module.mdm.dal.mysql.route.RouteOpMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.OP_DELETE_REFERENCED;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.OP_NOT_EXISTS;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.OP_TIME_MODEL_INVALID;
import static com.iboot.pf.module.mdm.enums.MdmCodeRuleConstants.OP;

/**
 * 标准工序 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class OpServiceImpl implements OpService {

    /**
     * 时长类型：按件累加
     */
    private static final String DURATION_TYPE_PER_UNIT = "PER_UNIT";

    @Resource
    private OpMapper opMapper;
    @Resource
    private RouteOpMapper routeOpMapper;
    @Resource
    private MdmCodeService mdmCodeService;

    @Override
    public Long createOp(OpSaveReqVO createReqVO) {
        validateTimeModel(createReqVO.getDurationType(), createReqVO.getStdMinutesPerUnit());
        // 插入
        OpDO op = BeanUtils.toBean(createReqVO, OpDO.class);
        op.setId(null);
        op.setCode(mdmCodeService.resolveCode(op.getCode(), OP));
        opMapper.insert(op);

        // 返回
        return op.getId();
    }

    @Override
    public void updateOp(OpSaveReqVO updateReqVO) {
        // 校验存在
        validateOpExists(updateReqVO.getId());
        validateTimeModel(updateReqVO.getDurationType(), updateReqVO.getStdMinutesPerUnit());
        // 更新
        OpDO updateObj = BeanUtils.toBean(updateReqVO, OpDO.class);
        opMapper.updateById(updateObj);
    }

    @Override
    public void deleteOp(Long id) {
        // 校验存在
        validateOpExists(id);
        // 校验引用：被工艺路线工序引用的工序不允许删除
        if (routeOpMapper.selectCountByOpId(id) > 0) {
            throw exception(OP_DELETE_REFERENCED);
        }
        // 删除
        opMapper.deleteById(id);
    }

    @Override
    public void deleteOpListByIds(List<Long> ids) {
        for (Long id : ids) {
            deleteOp(id);
        }
    }

    private void validateOpExists(Long id) {
        if (opMapper.selectById(id) == null) {
            throw exception(OP_NOT_EXISTS);
        }
    }

    /**
     * 校验工时模型：按件累加时单件加工工时必填。
     */
    private void validateTimeModel(String durationType, BigDecimal stdMinutesPerUnit) {
        if (DURATION_TYPE_PER_UNIT.equals(durationType)
                && (stdMinutesPerUnit == null || stdMinutesPerUnit.compareTo(BigDecimal.ZERO) < 0)) {
            throw exception(OP_TIME_MODEL_INVALID);
        }
    }

    @Override
    public OpDO getOp(Long id) {
        return opMapper.selectById(id);
    }

    @Override
    public PageResult<OpDO> getOpPage(OpPageReqVO pageReqVO) {
        return opMapper.selectPage(pageReqVO);
    }

}
