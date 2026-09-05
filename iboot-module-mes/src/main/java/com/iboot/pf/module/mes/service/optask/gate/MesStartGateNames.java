package com.iboot.pf.module.mes.service.optask.gate;

/**
 * 开工门禁五个校验项的名称。
 *
 * <p>集中定义而不是各实现类里写字面量：名称会进预检结果返回给前端，也会进日志，
 * 散在各处的字面量改一个名字就和历史记录、前端的展示映射对不上了。
 *
 * <p>取值与 {@code dev-docs/业务流程/04-MES生产执行流程.md} 的"开工门禁校验链"流程图一致。
 */
public interface MesStartGateNames {

    /** 前序完成。 */
    String PREV_DONE = "prevDone";

    /** 齐套或已发料。 */
    String KIT_OR_ISSUED = "kitOrIssued";

    /** 首件通过。 */
    String FIRST_ARTICLE = "firstArticle";

    /** 资源可用。 */
    String RESOURCE_AVAILABLE = "resourceAvailable";

    /** 技能与工装满足。 */
    String SKILL_AND_TOOL = "skillAndTool";

}
