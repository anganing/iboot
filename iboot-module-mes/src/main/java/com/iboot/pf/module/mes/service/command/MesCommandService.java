package com.iboot.pf.module.mes.service.command;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import com.iboot.pf.module.mes.enums.MesCommandTypeEnum;

import java.util.function.Supplier;

/**
 * MES 跨模块命令幂等服务。
 *
 * <p>APS 等上游可能重复下发同一条命令（超时重试、消息重投），界面上也可能连点两次。
 * 这里用幂等键做一次拦截：命中记录直接返回上一次的结果编号，不再执行业务动作。
 *
 * <p>只服务没有落库载体的状态推进型命令。创建型命令的业务表自带
 * {@code idempotency_key} 唯一索引，继续走列判重，不必经过这里。
 */
public interface MesCommandService {

    /**
     * 幂等执行有返回值的命令。
     *
     * @param commandType 命令类型
     * @param command     命令公共参数，含幂等键
     * @param action      业务动作
     * @return 结果编号；重复下发时返回首次执行的结果
     */
    Long execute(MesCommandTypeEnum commandType, MesCommandReqDTO command, Supplier<Long> action);

    /**
     * 幂等执行无返回值的命令。
     *
     * <p>工单的下达、挂起、关闭，工序的开工、暂停、完工、跳序都走这个入口：它们改的是状态，
     * 没有新编号可返回。
     *
     * @param commandType 命令类型
     * @param command     命令公共参数，含幂等键
     * @param action      业务动作
     */
    default void executeVoid(MesCommandTypeEnum commandType, MesCommandReqDTO command, Runnable action) {
        execute(commandType, command, () -> {
            action.run();
            return null;
        });
    }

}
