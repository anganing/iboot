package com.iboot.pf.module.wms.service.command;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.wms.api.dto.WmsCommandReqDTO;
import com.iboot.pf.module.wms.dal.dataobject.command.WmsCommandRecordDO;
import com.iboot.pf.module.wms.dal.mysql.command.WmsCommandRecordMapper;
import com.iboot.pf.module.wms.enums.WmsCommandTypeEnum;
import com.iboot.pf.module.wms.enums.WmsSourceModuleEnum;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 跨模块命令幂等服务测试。
 *
 * <p>覆盖：首次执行落记录、重复下发不二次执行、无返回值命令、动作抛异常时记录一起回滚、
 * 唯一索引拦住绕过前置查询的重复插入。
 */
@Import(WmsCommandServiceImpl.class)
class WmsCommandServiceImplTest extends BaseDbUnitTest {

    @Resource
    private WmsCommandService commandService;
    @Resource
    private WmsCommandRecordMapper commandRecordMapper;

    /** 测试用的命令参数。{@link WmsCommandReqDTO} 是抽象基类，这里给一个最小实现，不耦合具体业务 DTO。 */
    private static class TestCommand extends WmsCommandReqDTO {
    }

    private static TestCommand command(String key) {
        TestCommand cmd = new TestCommand();
        cmd.setIdempotencyKey(key);
        cmd.setSourceModule(WmsSourceModuleEnum.MES.name());
        cmd.setSourceBizNo("MR20260904001");
        cmd.setSourceBizLineNo("1");
        return cmd;
    }

    @Test
    void testExecute_firstTime() {
        // 准备参数
        TestCommand cmd = command("MES:MR20260904001:CONFIRM_OUT_ORDER:1");
        // 调用
        Long resultId = commandService.execute(WmsCommandTypeEnum.CONFIRM_OUT_ORDER, cmd, () -> 1024L);
        // 断言返回值
        assertEquals(1024L, resultId);
        // 断言记录已落库，且四个来源字段与命令一致
        WmsCommandRecordDO record = commandRecordMapper.selectByIdempotencyKey(cmd.getIdempotencyKey());
        assertNotNull(record);
        assertEquals(WmsCommandTypeEnum.CONFIRM_OUT_ORDER.name(), record.getCommandType());
        assertEquals(WmsSourceModuleEnum.MES.name(), record.getSourceModule());
        assertEquals("MR20260904001", record.getSourceBizNo());
        assertEquals("1", record.getSourceBizLineNo());
        assertEquals(1024L, record.getResultId());
    }

    @Test
    void testExecute_duplicated() {
        // 准备参数：同一个幂等键下发两次
        TestCommand cmd = command("MES:MR20260904002:CONFIRM_OUT_ORDER:1");
        AtomicInteger invoked = new AtomicInteger();
        // 调用两次
        Long first = commandService.execute(WmsCommandTypeEnum.CONFIRM_OUT_ORDER, cmd,
                () -> { invoked.incrementAndGet(); return 2048L; });
        Long second = commandService.execute(WmsCommandTypeEnum.CONFIRM_OUT_ORDER, cmd,
                () -> { invoked.incrementAndGet(); return 4096L; });
        // 断言第二次返回首次结果，业务动作只跑了一次
        assertEquals(2048L, first);
        assertEquals(2048L, second);
        assertEquals(1, invoked.get());
        // 断言只有一条记录
        assertEquals(1L, commandRecordMapper.selectCount(WmsCommandRecordDO::getIdempotencyKey,
                cmd.getIdempotencyKey()));
    }

    @Test
    void testExecuteVoid_duplicated() {
        // 准备参数：状态推进型命令没有结果编号
        TestCommand cmd = command("UI:IN20260904003:SUBMIT_IN_ORDER:0");
        AtomicInteger invoked = new AtomicInteger();
        // 调用两次
        commandService.executeVoid(WmsCommandTypeEnum.SUBMIT_IN_ORDER, cmd, invoked::incrementAndGet);
        commandService.executeVoid(WmsCommandTypeEnum.SUBMIT_IN_ORDER, cmd, invoked::incrementAndGet);
        // 断言动作只跑一次，记录的结果编号为空
        assertEquals(1, invoked.get());
        WmsCommandRecordDO record = commandRecordMapper.selectByIdempotencyKey(cmd.getIdempotencyKey());
        assertNotNull(record);
        assertNull(record.getResultId());
    }

    @Test
    void testExecute_actionFailed_noRecord() {
        // 准备参数：业务动作抛异常
        TestCommand cmd = command("MES:MR20260904004:CANCEL_OUT_ORDER:1");
        // 调用并断言异常抛出
        assertThrows(IllegalStateException.class, () ->
                commandService.execute(WmsCommandTypeEnum.CANCEL_OUT_ORDER, cmd, () -> {
                    throw new IllegalStateException("库存不足");
                }));
        // 断言没有落记录：失败的动作不能永久占住幂等键，否则重试会被判为已受理而直接返回
        assertNull(commandRecordMapper.selectByIdempotencyKey(cmd.getIdempotencyKey()));
    }

    @Test
    void testUniqueIndex_blocksDuplicateInsert() {
        // 准备参数：绕过 execute 的前置查询，直接插两条同键记录，模拟并发下两个线程都通过了前置查询
        String key = "MES:MR20260904005:RELEASE_RESERVATION:1";
        commandRecordMapper.insert(WmsCommandRecordDO.builder()
                .idempotencyKey(key)
                .commandType(WmsCommandTypeEnum.RELEASE_RESERVATION.name())
                .sourceModule(WmsSourceModuleEnum.MES.name())
                .sourceBizNo("MR20260904005")
                .sourceBizLineNo("1")
                .resultId(1L)
                .build());
        // 调用：第二条插入应被唯一索引拦住
        Exception ex = assertThrows(Exception.class, () -> commandRecordMapper.insert(
                WmsCommandRecordDO.builder()
                        .idempotencyKey(key)
                        .commandType(WmsCommandTypeEnum.RELEASE_RESERVATION.name())
                        .sourceModule(WmsSourceModuleEnum.MES.name())
                        .sourceBizNo("MR20260904005")
                        .sourceBizLineNo("1")
                        .resultId(2L)
                        .build()));
        // 断言是唯一约束冲突，不是别的错
        assertTrue(ex.getMessage() != null
                && (ex.getMessage().contains("Unique index") || ex.getMessage().contains("unique")
                    || ex.getMessage().contains("UK_WMS_COMMAND")
                    || ex.getMessage().toUpperCase().contains("DUPLICATE")),
                "期望唯一约束冲突，实际: " + ex.getMessage());
        // 断言库里仍只有第一条
        assertEquals(1L, commandRecordMapper.selectCount(WmsCommandRecordDO::getIdempotencyKey, key));
    }

}
