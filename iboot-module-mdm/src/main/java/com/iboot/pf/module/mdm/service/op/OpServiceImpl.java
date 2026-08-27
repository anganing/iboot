package com.iboot.pf.module.mdm.service.op;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import com.iboot.pf.module.mdm.controller.admin.op.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.op.OpDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mdm.dal.mysql.op.OpMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.*;
import static com.iboot.pf.module.mdm.enums.MdmCodeRuleConstants.OP;

/**
 * 标准工序 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class OpServiceImpl implements OpService {

    @Resource
    private OpMapper opMapper;
    @Resource
    private com.iboot.pf.module.mdm.service.coderule.MdmCodeService mdmCodeService;

    @Override
    public Long createOp(OpSaveReqVO createReqVO) {
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
        // 更新
        OpDO updateObj = BeanUtils.toBean(updateReqVO, OpDO.class);
        opMapper.updateById(updateObj);
    }

    @Override
    public void deleteOp(Long id) {
        // 校验存在
        validateOpExists(id);
        // 删除
        opMapper.deleteById(id);
    }

    @Override
        public void deleteOpListByIds(List<Long> ids) {
        // 删除
        opMapper.deleteByIds(ids);
        }


    private void validateOpExists(Long id) {
        if (opMapper.selectById(id) == null) {
            throw exception(OP_NOT_EXISTS);
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