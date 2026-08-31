package com.iboot.pf.module.aps.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 跨模块命令的来源模块，写进 {@code aps_command_record.source_module}。 */
@Getter
@AllArgsConstructor
public enum ApsSourceModuleEnum implements ArrayValuable<String> {

    /** 制造执行。 */
    MES("制造执行"),
    /** 高级排程自身。 */
    APS("高级排程"),
    /** 外部集成。 */
    INTEGRATION("外部集成"),
    /** 管理后台人工操作。 */
    UI("管理后台");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
