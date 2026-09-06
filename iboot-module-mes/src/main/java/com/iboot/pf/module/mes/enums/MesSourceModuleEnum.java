package com.iboot.pf.module.mes.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 调用方模块。{@code mes_command_record.source_module} 与幂等键的第一段都取自这里。
 *
 * <p>取值集合与 {@link com.iboot.pf.module.mes.api.dto.MesCommandReqDTO} 的字段注释一致，
 * 也与 WMS 侧的 {@code WmsSourceModuleEnum} 同构：跨模块命令在两个模块的记录里应当用同一批标识，
 * 否则同一条链路在两侧的流水按来源过滤会得到不同结果。
 *
 * <p>管理后台的动作端点一律用 {@link #UI}：端点是人在界面上点出来的，来源就是后台人工。
 * 领域 Service 里那几处 {@code SOURCE_MODULE_MES = "MES"} 私有常量是本枚举之前的写法，
 * 语义相同，后续应统一到这里。
 */
@Getter
@AllArgsConstructor
public enum MesSourceModuleEnum implements ArrayValuable<String> {

    /** MES 制造执行自身。 */
    MES("MES 制造执行"),
    /** APS 高级排程。 */
    APS("APS 高级排程"),
    /** 外部集成。 */
    INTEGRATION("外部集成"),
    /** 后台人工，管理后台的动作端点用它。 */
    UI("后台人工");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
