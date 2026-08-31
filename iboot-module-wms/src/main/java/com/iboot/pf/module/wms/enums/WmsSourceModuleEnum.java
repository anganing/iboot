package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 调用方模块。跨模块命令的幂等键前缀与来源类型都取自这里。 */
@Getter
@AllArgsConstructor
public enum WmsSourceModuleEnum implements ArrayValuable<String> {

    /** MES 制造执行。 */
    MES("MES 制造执行"),
    /** APS 高级排程。 */
    APS("APS 高级排程"),
    /** WMS 仓储管理自身。 */
    WMS("WMS 仓储管理"),
    /** 外部集成。 */
    INTEGRATION("外部集成"),
    /** 后台人工。 */
    UI("后台人工");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
