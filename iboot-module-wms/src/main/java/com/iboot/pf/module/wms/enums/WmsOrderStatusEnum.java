package com.iboot.pf.module.wms.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * WMS 单据状态。
 *
 * <p>入库、出库、转移、盘点共用同一套状态码，各单据只使用其中与自身业务相符的子集：
 * <ul>
 *     <li>入库单：DRAFT、SUBMITTED、QC_PENDING、READY、TASK_CREATED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION</li>
 *     <li>出库单：DRAFT、SUBMITTED、READY、TASK_CREATED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION</li>
 *     <li>转移单：DRAFT、SUBMITTED、APPROVED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION</li>
 *     <li>盘点单：DRAFT、FROZEN、COUNTING、DIFF_CONFIRMED、DONE、CANCELLED</li>
 * </ul>
 *
 * <p>跨模块命令只认“新建 / 已确认 / 已过账 / 已取消”四种语义：
 * 新建=DRAFT，已确认=SUBMITTED，已过账=DONE，已取消=CANCELLED。
 */
@Getter
@AllArgsConstructor
public enum WmsOrderStatusEnum {

    /** 新建。 */
    DRAFT("新建"),
    /** 已确认。 */
    SUBMITTED("已确认"),
    /** 待质检。 */
    QC_PENDING("待质检"),
    /** 已审批。 */
    APPROVED("已审批"),
    /** 已冻结账面。 */
    FROZEN("已冻结账面"),
    /** 待作业。 */
    READY("待作业"),
    /** 任务已创建。 */
    TASK_CREATED("任务已创建"),
    /** 作业中。 */
    IN_PROGRESS("作业中"),
    /** 盘点中。 */
    COUNTING("盘点中"),
    /** 差异已确认。 */
    DIFF_CONFIRMED("差异已确认"),
    /** 已过账。 */
    DONE("已过账"),
    /** 已取消。 */
    CANCELLED("已取消"),
    /** 异常。 */
    EXCEPTION("异常");

    private final String name;

    /** 是否新建：只有新建状态允许取消。 */
    public static boolean isDraft(String status) {
        return ObjUtil.equal(DRAFT.name(), status);
    }

    /** 是否已取消。 */
    public static boolean isCancelled(String status) {
        return ObjUtil.equal(CANCELLED.name(), status);
    }

    /** 是否可过账：新建或已确认。 */
    public static boolean canPost(String status) {
        return isDraft(status) || ObjUtil.equal(SUBMITTED.name(), status);
    }

}
