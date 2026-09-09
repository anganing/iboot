package com.iboot.pf.module.mes.service.qcitem;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mes.controller.admin.qcitem.vo.QcItemPageReqVO;
import com.iboot.pf.module.mes.controller.admin.qcitem.vo.QcItemRespVO;
import com.iboot.pf.module.mes.controller.admin.qcitem.vo.QcItemSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.qcitem.QcItemDO;
import com.iboot.pf.module.mes.dal.dataobject.qcplan.QcPlanDO;
import com.iboot.pf.module.mes.dal.mysql.qcitem.QcItemMapper;
import com.iboot.pf.module.mes.enums.MesQcItemTypeEnum;
import com.iboot.pf.module.mes.enums.MesQcPlanStatusEnum;
import com.iboot.pf.module.mes.service.qcplan.QcPlanService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_ITEM_CODE_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_ITEM_LIMIT_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_ITEM_LIMIT_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_ITEM_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_ITEM_PLAN_NOT_DRAFT;

/**
 * MES-检验项目 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class QcItemServiceImpl implements QcItemService {

    @Resource
    private QcItemMapper qcItemMapper;
    @Resource
    private QcPlanService qcPlanService;

    @Override
    public Long createQcItem(QcItemSaveReqVO createReqVO) {
        validatePlanDraft(createReqVO.getPlanId());
        QcItemDO qcItem = BeanUtils.toBean(createReqVO, QcItemDO.class);
        qcItem.setId(null);
        validateLimit(qcItem);
        validateItemCodeUnique(qcItem, null);
        qcItemMapper.insert(qcItem);
        return qcItem.getId();
    }

    @Override
    public void updateQcItem(QcItemSaveReqVO updateReqVO) {
        QcItemDO existed = validateQcItemExists(updateReqVO.getId());
        // 原所属方案与目标方案都要是草稿：只查目标方案的话，已发布方案下的检验项
        // 可以靠把 planId 改成草稿方案"合法搬走"，等于篡改已发布方案的检验内容
        validatePlanDraft(existed.getPlanId());
        validatePlanDraft(updateReqVO.getPlanId());
        QcItemDO qcItem = BeanUtils.toBean(updateReqVO, QcItemDO.class);
        validateLimit(qcItem);
        // 改自己不能撞自己，所以排除自身编号
        validateItemCodeUnique(qcItem, updateReqVO.getId());
        qcItemMapper.updateById(qcItem);
    }

    @Override
    public void deleteQcItem(Long id) {
        QcItemDO existed = validateQcItemExists(id);
        validatePlanDraft(existed.getPlanId());
        qcItemMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQcItemListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            QcItemDO existed = validateQcItemExists(id);
            validatePlanDraft(existed.getPlanId());
        }
        qcItemMapper.deleteByIds(ids);
    }

    private QcItemDO validateQcItemExists(Long id) {
        QcItemDO qcItem = id == null ? null : qcItemMapper.selectById(id);
        if (qcItem == null) {
            throw exception(QC_ITEM_NOT_EXISTS);
        }
        return qcItem;
    }

    /**
     * 只有草稿状态的方案允许维护检验项。
     *
     * <p>已发布方案会被质检任务锁定引用，改检验项等于改历史任务的判定口径。
     */
    private void validatePlanDraft(Long planId) {
        QcPlanDO qcPlan = qcPlanService.validateQcPlanExists(planId);
        if (!MesQcPlanStatusEnum.isDraft(qcPlan.getStatus())) {
            throw exception(QC_ITEM_PLAN_NOT_DRAFT);
        }
    }

    /** 数值型检验项必须给出上限或下限，且上限不能小于下限，否则录入结果时无法判定合格。 */
    private void validateLimit(QcItemDO qcItem) {
        if (!MesQcItemTypeEnum.isNumeric(qcItem.getItemType())) {
            return;
        }
        if (qcItem.getUpperLimit() == null && qcItem.getLowerLimit() == null) {
            throw exception(QC_ITEM_LIMIT_REQUIRED);
        }
        if (qcItem.getUpperLimit() != null && qcItem.getLowerLimit() != null
                && qcItem.getUpperLimit().compareTo(qcItem.getLowerLimit()) < 0) {
            throw exception(QC_ITEM_LIMIT_INVALID);
        }
    }

    /** 同一方案下检验项编码唯一，对应唯一索引 (plan_id, item_code, deleted)。 */
    private void validateItemCodeUnique(QcItemDO qcItem, Long excludeId) {
        QcItemDO existed = qcItemMapper.selectByPlanIdAndItemCode(qcItem.getPlanId(), qcItem.getItemCode());
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(QC_ITEM_CODE_DUPLICATE);
    }

    @Override
    public QcItemRespVO getQcItem(Long id) {
        return qcItemMapper.selectJoinById(id);
    }

    @Override
    public PageResult<QcItemRespVO> getQcItemPage(QcItemPageReqVO pageReqVO) {
        return qcItemMapper.selectPage(pageReqVO);
    }

}
