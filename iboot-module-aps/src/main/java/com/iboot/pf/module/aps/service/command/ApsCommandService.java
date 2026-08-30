package com.iboot.pf.module.aps.service.command;

import com.iboot.pf.module.aps.api.dto.ApsCommandReqDTO;
import java.util.function.Supplier;

/** APS 跨模块命令幂等服务。 */
public interface ApsCommandService {
    Long execute(String commandType, ApsCommandReqDTO command, Supplier<Long> action);
    default void executeVoid(String commandType, ApsCommandReqDTO command, Runnable action) {
        execute(commandType, command, () -> { action.run(); return null; });
    }
}
