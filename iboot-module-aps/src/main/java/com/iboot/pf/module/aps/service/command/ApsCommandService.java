package com.iboot.pf.module.aps.service.command;

import com.iboot.pf.module.aps.api.dto.ApsCommandReqDTO;
import com.iboot.pf.module.aps.enums.ApsCommandTypeEnum;

import java.util.function.Supplier;

/**
 * APS 跨模块命令幂等服务。
 *
 * <p>MES 等上游可能重复下发同一条命令（超时重试、消息重投），这里用幂等键做一次拦截：
 * 命中记录直接返回上一次的结果编号，不再执行业务动作。
 */
public interface ApsCommandService {

    /**
     * 幂等执行有返回值的命令
     *
     * @param commandType 命令类型
     * @param command     命令公共参数，含幂等键
     * @param action      业务动作
     * @return 结果编号；重复下发时返回首次执行的结果
     */
    Long execute(ApsCommandTypeEnum commandType, ApsCommandReqDTO command, Supplier<Long> action);

    /**
     * 幂等执行无返回值的命令
     *
     * @param commandType 命令类型
     * @param command     命令公共参数，含幂等键
     * @param action      业务动作
     */
    default void executeVoid(ApsCommandTypeEnum commandType, ApsCommandReqDTO command, Runnable action) {
        execute(commandType, command, () -> {
            action.run();
            return null;
        });
    }

}
