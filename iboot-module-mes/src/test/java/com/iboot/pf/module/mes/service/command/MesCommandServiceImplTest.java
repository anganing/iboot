package com.iboot.pf.module.mes.service.command;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import com.iboot.pf.module.mes.dal.dataobject.command.MesCommandRecordDO;
import com.iboot.pf.module.mes.dal.mysql.command.MesCommandRecordMapper;
import com.iboot.pf.module.mes.enums.MesCommandTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

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
@Import(MesCommandServiceImpl.class)
class MesCommandServiceImplTest extends BaseDbUnitTest {

    /** 来源模块取值，MES 侧没有对应枚举，与 OpTaskServiceImplTest 的写法保持一致。 */
    private static final String SOURCE_UI = "UI";
    private static final String SOURCE_APS = "APS";

    @Resource
    private MesCommandService commandService;
    @Resource
    private MesCommandRecordMapper commandRecordMapper;

    /** 测试用的命令参数。{@link MesCommandReqDTO} 是抽象基类，这里给一个最小实现，不耦合具体业务 DTO。 */
    private static class TestCommand extends MesCommandReqDTO {
    }

    private static TestCommand command(String key, String sourceModule, String bizNo, String lineNo) {
        TestCommand cmd = new TestCommand();
        cmd.setIdempotencyKey(key);
        cmd.setSourceModule(sourceModule);
        cmd.setSourceBizNo(bizNo);
        cmd.setSourceBizLineNo(lineNo);
        return cmd;
    }

    @Test
    void testExecute_firstTime() {
        // 准备参数
        TestCommand cmd = command("APS:SV20260904001:DEMAND_TO_ORDER:0", SOURCE_APS, "SV20260904001", "0");
        // 调用
        Long resultId = commandService.execute(MesCommandTypeEnum.DEMAND_TO_ORDER, cmd, () -> 1024L);
        // 断言返回值
        assertEquals(1024L, resultId);
        // 断言记录已落库，且四个来源字段与命令一致
        MesCommandRecordDO record = commandRecordMapper.selectByIdempotencyKey(cmd.getIdempotencyKey());
        assertNotNull(record);
        assertEquals(MesCommandTypeEnum.DEMAND_TO_ORDER.name(), record.getCommandType());
        assertEquals(SOURCE_APS, record.getSourceModule());
        assertEquals("SV20260904001", record.getSourceBizNo());
        assertEquals("0", record.getSourceBizLineNo());
        assertEquals(1024L, record.getResultId());
    }

    @Test
    void testExecute_duplicated() {
        // 准备参数：同一个幂等键下发两次
        TestCommand cmd = command("APS:SV20260904002:DEMAND_TO_ORDER:0", SOURCE_APS, "SV20260904002", "0");
        AtomicInteger invoked = new AtomicInteger();
        // 调用两次
        Long first = commandService.execute(MesCommandTypeEnum.DEMAND_TO_ORDER, cmd,
                () -> { invoked.incrementAndGet(); return 2048L; });
        Long second = commandService.execute(MesCommandTypeEnum.DEMAND_TO_ORDER, cmd,
                () -> { invoked.incrementAndGet(); return 4096L; });
        // 断言第二次返回首次结果，业务动作只跑了一次
        assertEquals(2048L, first);
        assertEquals(2048L, second);
        assertEquals(1, invoked.get());
        // 断言只有一条记录
        assertEquals(1L, commandRecordMapper.selectCount(MesCommandRecordDO::getIdempotencyKey,
                cmd.getIdempotencyKey()));
    }

    @Test
    void testExecuteVoid_duplicated() {
        // 准备参数：状态推进型命令没有结果编号
        TestCommand cmd = command("UI:PO20260904003:RELEASE_PROD_ORDER:0", SOURCE_UI, "PO20260904003", null);
        AtomicInteger invoked = new AtomicInteger();
        // 调用两次
        commandService.executeVoid(MesCommandTypeEnum.RELEASE_PROD_ORDER, cmd, invoked::incrementAndGet);
        commandService.executeVoid(MesCommandTypeEnum.RELEASE_PROD_ORDER, cmd, invoked::incrementAndGet);
        // 断言动作只跑一次，记录的结果编号为空
        assertEquals(1, invoked.get());
        MesCommandRecordDO record = commandRecordMapper.selectByIdempotencyKey(cmd.getIdempotencyKey());
        assertNotNull(record);
        assertNull(record.getResultId());
    }

    @Test
    void testExecute_actionFailed_noRecord() {
        // 准备参数：业务动作抛异常
        TestCommand cmd = command("UI:OPT20260904004:START_OP_TASK:0", SOURCE_UI, "OPT20260904004", null);
        // 调用并断言异常抛出
        assertThrows(IllegalStateException.class, () ->
                commandService.execute(MesCommandTypeEnum.START_OP_TASK, cmd, () -> {
                    throw new IllegalStateException("前序工序未完工");
                }));
        // 断言没有落记录：失败的动作不能永久占住幂等键，否则重试会被判为已受理而直接返回
        assertNull(commandRecordMapper.selectByIdempotencyKey(cmd.getIdempotencyKey()));
    }

    @Test
    void testUniqueIndex_blocksDuplicateInsert() {
        // 准备参数：绕过 execute 的前置查询，直接插两条同键记录，模拟并发下两个线程都通过了前置查询
        String key = "UI:OPT20260904005:FINISH_OP_TASK:0";
        commandRecordMapper.insert(buildRecord(key, 1L));
        // 调用：第二条插入应被唯一索引拦住
        Exception ex = assertThrows(Exception.class, () -> commandRecordMapper.insert(buildRecord(key, 2L)));
        // 断言是唯一约束冲突，不是别的错
        assertTrue(ex.getMessage() != null
                && (ex.getMessage().contains("Unique index") || ex.getMessage().contains("unique")
                    || ex.getMessage().contains("UK_MES_COMMAND")
                    || ex.getMessage().toUpperCase().contains("DUPLICATE")),
                "期望唯一约束冲突，实际: " + ex.getMessage());
        // 断言库里仍只有第一条
        assertEquals(1L, commandRecordMapper.selectCount(MesCommandRecordDO::getIdempotencyKey, key));
    }

    private static MesCommandRecordDO buildRecord(String key, Long resultId) {
        return MesCommandRecordDO.builder()
                .idempotencyKey(key)
                .commandType(MesCommandTypeEnum.FINISH_OP_TASK.name())
                .sourceModule(SOURCE_UI)
                .sourceBizNo("OPT20260904005")
                .resultId(resultId)
                .build();
    }

}
