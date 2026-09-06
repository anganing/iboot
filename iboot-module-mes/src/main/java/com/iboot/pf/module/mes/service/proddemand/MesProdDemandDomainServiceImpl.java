package com.iboot.pf.module.mes.service.proddemand;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import com.iboot.pf.module.mes.dal.dataobject.proddemand.ProdDemandDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.mysql.proddemand.ProdDemandMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.enums.MesCodeRuleConstants;
import com.iboot.pf.module.mes.enums.MesCommandTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesDemandStatusEnum;
import com.iboot.pf.module.mes.enums.MesKitStatusEnum;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.command.MesCommandService;
import com.iboot.pf.module.mes.service.proddemand.bo.MesConfirmDemandBO;
import com.iboot.pf.module.mes.service.proddemand.bo.MesDemandToOrderBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertMap;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_DEMAND_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_DEMAND_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_DEMAND_TO_ORDER_QTY_EXCEEDED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QTY_MUST_BE_POSITIVE;

/**
 * 生产需求领域动作实现。
 *
 * <p>三个动作都是同一个骨架：取需求、派生幂等键、在 {@link MesCommandService} 的幂等壳里校验状态并改状态。
 * 状态校验放在幂等壳内而不是壳外：壳外校验会让重复提交在"状态已被首次调用改掉"时抛非法状态错误，
 * 把幂等变成报错。
 *
 * <p>需求域不写 {@code mes_wip_txn}：那张表的 {@code prod_order_id} 是必填主维度，记的是在制品的流转轨迹，
 * 而需求阶段还没有在制品。工单的第一条流水由下达动作写（{@code DRAFT} 到 {@code RELEASED}）。
 */
@Service
public class MesProdDemandDomainServiceImpl implements MesProdDemandDomainService {

    /** 幂等键的段分隔符，四段格式 {@code {来源模块}:{单据编号}:{命令类型}:{行号}}。 */
    private static final String KEY_SEPARATOR = ":";

    /** 幂等键的行号段：确认与取消都作用在单据头上，没有行号，固定 0。 */
    private static final String KEY_HEAD_LINE_NO = "0";

    @Resource
    private ProdDemandMapper prodDemandMapper;
    @Resource
    private ProdOrderMapper prodOrderMapper;
    @Resource
    private MesCodeService codeService;
    @Resource
    private MesCommandService commandService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmDemand(MesConfirmDemandBO bo) {
        ProdDemandDO demand = requireDemand(bo.getDemandId());
        MesCommandReqDTO command = buildCommand(bo, MesCommandTypeEnum.CONFIRM_DEMAND,
                demand.getDemandNo(), KEY_HEAD_LINE_NO);
        commandService.executeVoid(MesCommandTypeEnum.CONFIRM_DEMAND, command, () -> {
            // 只有新建状态可以确认：已排产的需求被"重新确认"会把排程已纳入这件事抹掉，
            // 已下达的需求下面已经挂着工单，退回未确认没有任何业务含义
            if (!MesDemandStatusEnum.isDraft(demand.getStatus())) {
                throw exception(PROD_DEMAND_STATUS_INVALID);
            }
            updateStatus(demand, MesDemandStatusEnum.CONFIRMED, bo.getRemark());
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelDemand(MesConfirmDemandBO bo) {
        ProdDemandDO demand = requireDemand(bo.getDemandId());
        MesCommandReqDTO command = buildCommand(bo, MesCommandTypeEnum.CANCEL_DEMAND,
                demand.getDemandNo(), KEY_HEAD_LINE_NO);
        commandService.executeVoid(MesCommandTypeEnum.CANCEL_DEMAND, command, () -> {
            // 终态之外都可取消（04 文档"任一非终态都可取消"）。已完成与已取消不能再动：
            // 前者的账已经随工单关闭收尾，后者重复取消没有意义
            if (MesDemandStatusEnum.isFinal(demand.getStatus())) {
                throw exception(PROD_DEMAND_STATUS_INVALID);
            }
            updateStatus(demand, MesDemandStatusEnum.CANCELLED, bo.getRemark());
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long demandToProdOrder(MesDemandToOrderBO bo) {
        ProdDemandDO demand = requireDemand(bo.getDemandId());
        if (bo.getQty() == null || bo.getQty().compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(QTY_MUST_BE_POSITIVE);
        }
        // 本需求已生成的工单：既用来算累计转单量，也用来给幂等键末段发号，让分批转单天然不撞键
        List<ProdOrderDO> existedOrders = prodOrderMapper.selectList(ProdOrderDO::getDemandId, demand.getId());
        MesCommandReqDTO command = buildCommand(bo, MesCommandTypeEnum.DEMAND_TO_ORDER,
                demand.getDemandNo(), String.valueOf(existedOrders.size()));
        return commandService.execute(MesCommandTypeEnum.DEMAND_TO_ORDER, command, () -> {
            // 已确认与已排产都能转：排产只是给了计划时间与资源，不改变"这批需求可以投产"这件事
            if (!canToProdOrder(demand.getStatus())) {
                throw exception(PROD_DEMAND_STATUS_INVALID);
            }
            BigDecimal afterQty = sumConvertedQty(existedOrders).add(bo.getQty());
            if (afterQty.compareTo(demand.getQty()) > 0) {
                throw exception(PROD_DEMAND_TO_ORDER_QTY_EXCEEDED);
            }
            Long prodOrderId = createProdOrder(demand, bo);
            // 转满才置已下达。没转满时保持当前状态不变（已确认停在已确认、已排产停在已排产）：
            // 把已排产改回已确认会让 APS 以为这批需求还没被纳入排程，下一轮又排一次
            if (afterQty.compareTo(demand.getQty()) == 0) {
                updateStatus(demand, MesDemandStatusEnum.RELEASED, null);
            }
            return prodOrderId;
        });
    }

    @Override
    public void validateDemandDraft(Long demandId) {
        ProdDemandDO demand = requireDemand(demandId);
        requireDraft(demand);
    }

    @Override
    public void validateDemandDraft(Collection<Long> demandIds) {
        if (CollUtil.isEmpty(demandIds)) {
            return;
        }
        // 一次查回全部需求，不按 ID 逐个查库：批量删一次点几十条是常态
        Map<Long, ProdDemandDO> demandMap = convertMap(prodDemandMapper.selectByIds(demandIds),
                ProdDemandDO::getId);
        for (Long demandId : demandIds) {
            ProdDemandDO demand = demandMap.get(demandId);
            if (demand == null) {
                throw exception(PROD_DEMAND_NOT_EXISTS);
            }
            requireDraft(demand);
        }
    }

    /**
     * 只有新建状态的需求允许表单改动与删除。
     *
     * <p>确认之后需求就是投产承诺：已确认的改数量会让转出去的工单与需求对不上，已排产的改交期会让 APS
     * 上一轮的排程结果失效，已下达的下面已经挂着工单。这三档 codegen Service 的终态判断都放过去了。
     */
    private void requireDraft(ProdDemandDO demand) {
        if (!MesDemandStatusEnum.isDraft(demand.getStatus())) {
            throw exception(PROD_DEMAND_STATUS_INVALID);
        }
    }

    /**
     * 按需求快照建一张新建状态的生产工单。
     *
     * <p>不走 codegen 的 {@code ProdOrderService.createProdOrder}：它的入参 VO 强制 {@code bomId} 与
     * {@code routeId} 非空，并且会回查两者的版本快照。按 04 文档"为什么必须锁版本"的口径，BOM 与工艺版本
     * 要等下达那一刻才锁，转单时提前锁等于把下达动作的职责挪到这里，主数据在转单到下达之间改版就锁错了。
     * 因此这里直插 DO，四个版本字段先占位，留给下达动作填。
     */
    private Long createProdOrder(ProdDemandDO demand, MesDemandToOrderBO bo) {
        ProdOrderDO prodOrder = ProdOrderDO.builder()
                .orderNo(codeService.generate(MesCodeRuleConstants.PROD_ORDER, null))
                .demandId(demand.getId())
                // 工厂、项目、物料、单位、交期、优先级全部快照需求：主数据后续改动不影响已投产的工单
                .plantId(demand.getPlantId())
                .workshopId(bo.getWorkshopId() == null ? MesConstants.NONE_ID : bo.getWorkshopId())
                .projectId(demand.getProjectId())
                .projectTaskId(demand.getProjectTaskId())
                .matId(demand.getMatId())
                .matCode(demand.getMatCode())
                .matName(demand.getMatName())
                .matSpec(demand.getMatSpec())
                .uomId(demand.getUomId())
                .uomCode(demand.getUomCode())
                .dueDate(demand.getDueDate())
                .priority(demand.getPriority())
                // 数量取本次转单量而不是需求量，部分转单靠的就是这一处
                .qty(bo.getQty())
                // 进度数量由报工推进，齐套状态由齐套检查写入，建单时归零
                .completedQty(MesConstants.ZERO_QTY)
                .scrapQty(MesConstants.ZERO_QTY)
                .kitStatus(MesKitStatusEnum.UNCHECKED.name())
                // 四个版本快照列在库里是 NOT NULL，下达动作锁版本之前先占位
                .bomId(MesConstants.NONE_ID)
                .bomVersion(StrUtil.EMPTY)
                .routeId(MesConstants.NONE_ID)
                .routeVersion(StrUtil.EMPTY)
                .status(MesProdOrderStatusEnum.DRAFT.name())
                .version(MesConstants.INIT_VERSION)
                .remark(bo.getRemark())
                .build();
        prodOrderMapper.insert(prodOrder);
        return prodOrder.getId();
    }

    /**
     * 累计已转单量：本需求下未取消工单的计划数量之和。
     *
     * <p>刻意不在 {@code mes_prod_demand} 上加一列"累计转单量"：工单表已经是这个事实的唯一来源，
     * 多存一份就有两处真相要维护，工单被取消时最容易漏更新。一个需求分批投产的工单是个位数，汇总的代价可忽略。
     *
     * <p>已取消的工单不计入：那批量没有真的投产，不排除的话需求会永远转不满、卡在已确认。
     */
    private BigDecimal sumConvertedQty(List<ProdOrderDO> orders) {
        return orders.stream()
                .filter(order -> !ObjUtil.equal(MesProdOrderStatusEnum.CANCELLED.name(), order.getStatus()))
                .map(order -> order.getQty() == null ? BigDecimal.ZERO : order.getQty())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** 已确认或已排产才允许转工单。 */
    private boolean canToProdOrder(String status) {
        return ObjUtil.equal(MesDemandStatusEnum.CONFIRMED.name(), status)
                || ObjUtil.equal(MesDemandStatusEnum.PLANNED.name(), status);
    }

    /**
     * 改需求状态。
     *
     * <p>只写主键、状态与备注三列，不用 {@code updateById(demand)} 整体回写：整体回写会把内存里这份可能已经
     * 陈旧的副本盖到库上，把并发下别人刚改的字段冲掉。
     *
     * <p>顺手把内存对象的状态同步过来，同一事务内后续判断不会读到旧状态。
     */
    private void updateStatus(ProdDemandDO demand, MesDemandStatusEnum target, String remark) {
        ProdDemandDO update = new ProdDemandDO().setId(demand.getId()).setStatus(target.name());
        if (StrUtil.isNotBlank(remark)) {
            update.setRemark(remark);
        }
        prodDemandMapper.updateById(update);
        demand.setStatus(target.name());
    }

    private ProdDemandDO requireDemand(Long demandId) {
        ProdDemandDO demand = demandId == null ? null : prodDemandMapper.selectById(demandId);
        if (demand == null) {
            throw exception(PROD_DEMAND_NOT_EXISTS);
        }
        return demand;
    }

    /**
     * 组装幂等命令。
     *
     * <p>幂等键调用方给了就以它为准，留空时按四段格式 {@code {来源模块}:{需求编号}:{命令类型}:{行号}} 派生。
     * 动作段取 {@link MesCommandTypeEnum} 的枚举名而不是写字面量：键会落进 {@code mes_command_record}，
     * 哪天有人改了命令名，字面量拼出来的历史键就与枚举对不上了。
     *
     * <p>来源单号留空时兜底成需求编号，因为 {@code mes_command_record.source_biz_no} 是 NOT NULL。
     */
    private MesCommandReqDTO buildCommand(MesCommandReqDTO source, MesCommandTypeEnum commandType,
                                          String demandNo, String lineNo) {
        // 来源模块是幂等键的第一段，也是排查"这条命令谁下的"的唯一线索，漏传属于调用方契约违约
        Assert.notBlank(source.getSourceModule(), "来源模块不能为空，它是幂等键的第一段");
        DemandCommand command = new DemandCommand();
        command.setSourceModule(source.getSourceModule());
        command.setSourceBizNo(StrUtil.isBlank(source.getSourceBizNo()) ? demandNo : source.getSourceBizNo());
        command.setSourceBizLineNo(source.getSourceBizLineNo());
        command.setIdempotencyKey(StrUtil.isBlank(source.getIdempotencyKey())
                ? String.join(KEY_SEPARATOR, source.getSourceModule(), demandNo, commandType.name(), lineNo)
                : source.getIdempotencyKey());
        return command;
    }

    /**
     * 幂等命令载体。
     *
     * <p>{@link MesCommandReqDTO} 是抽象类，需求域给它一个最小实现：既不必新建一个公共 DTO，
     * 也不用把派生出来的幂等键写回调用方的 BO——改调用方传进来的对象是隐蔽的副作用。
     */
    private static class DemandCommand extends MesCommandReqDTO {
    }

}
