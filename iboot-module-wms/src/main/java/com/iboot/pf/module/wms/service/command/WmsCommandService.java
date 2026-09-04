package com.iboot.pf.module.wms.service.command;

import com.iboot.pf.module.wms.api.dto.WmsCommandReqDTO;
import com.iboot.pf.module.wms.enums.WmsCommandTypeEnum;

import java.util.function.Supplier;

/**
 * WMS 跨模块命令幂等服务。
 *
 * <p>MES 等上游可能重复下发同一条命令（超时重试、消息重投），界面上也可能连点两次。
 * 这里用幂等键做一次拦截：命中记录直接返回上一次的结果编号，不再执行业务动作。
 *
 * <p>只服务没有落库载体的状态推进型命令。创建型命令的单据表自带
 * {@code idempotency_key} 唯一索引，继续走列判重，不必经过这里。
 */
public interface WmsCommandService {

    /**
     * 幂等执行有返回值的命令。
     *
     * @param commandType 命令类型
     * @param command     命令公共参数，含幂等键
     * @param action      业务动作
     * @return 结果编号；重复下发时返回首次执行的结果
     */
    Long execute(WmsCommandTypeEnum commandType, WmsCommandReqDTO command, Supplier<Long> action);

    /**
     * 幂等执行无返回值的命令。
     *
     * <p>单据的提交、确认、取消都走这个入口：它们改的是状态，没有新编号可返回。
     *
     * @param commandType 命令类型
     * @param command     命令公共参数，含幂等键
     * @param action      业务动作
     */
    default void executeVoid(WmsCommandTypeEnum commandType, WmsCommandReqDTO command, Runnable action) {
        execute(commandType, command, () -> {
            action.run();
            return null;
        });
    }

}
