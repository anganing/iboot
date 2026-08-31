package com.iboot.pf.module.mes.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 领退补料申请状态。与字典 {@code mes_material_req_status} 一致。
 *
 * <p>提交后由 WMS 生成出入库单并回写处理数量，MES 侧只负责推进状态。
 */
@Getter
@AllArgsConstructor
public enum MesMaterialReqStatusEnum implements ArrayValuable<String> {

    /** 新建。 */
    DRAFT("新建"),
    /** 已提交。 */
    SUBMITTED("已提交"),
    /** WMS处理中。 */
    WMS_PROCESSING("WMS处理中"),
    /** 部分完成。 */
    PARTIAL("部分完成"),
    /** 已完成。 */
    DONE("已完成"),
    /** 已取消。 */
    CANCELLED("已取消");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 是否新建：只有新建状态允许改动申请与明细。 */
    public static boolean isDraft(String status) {
        return ObjUtil.equal(DRAFT.name(), status);
    }

    /** 是否已交给 WMS：交出去之后明细不能再改，否则两侧数量对不上。 */
    public static boolean isHandedToWms(String status) {
        return ObjUtil.equal(WMS_PROCESSING.name(), status)
                || ObjUtil.equal(PARTIAL.name(), status)
                || ObjUtil.equal(DONE.name(), status);
    }

}
