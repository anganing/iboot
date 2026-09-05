package com.iboot.pf.module.mes.enums;

import cn.hutool.core.util.ObjUtil;

/**
 * 供料方式。取值与 {@code mdm_bom_line.issue_method}、{@code mdm_route_op_mat.issue_method} 一致。
 *
 * <p>定义在 MES 侧而不是 mdm 侧：mdm 只负责存这个字段，怎么解释它是 MES 的业务规则。
 * 齐套算不算需求、领料领不领、库存什么时候扣，三种方式的行为差异见
 * {@code dev-docs/业务流程/04-MES生产执行流程.md} 的"三种发料方式的行为差异"：
 *
 * <table>
 *     <tr><th>方式</th><th>齐套计需求</th><th>领料</th><th>库存扣减时机</th></tr>
 *     <tr><td>PICK / DIRECT</td><td>计</td><td>领</td><td>领料出库过账时</td></tr>
 *     <tr><td>PHANTOM</td><td>不计，向下展开子件</td><td>不领</td><td>子件被领时</td></tr>
 *     <tr><td>BACKFLUSH</td><td>不计</td><td>不领</td><td>报工按实际产量反冲</td></tr>
 * </table>
 *
 * <p>把 {@code PHANTOM} 当常规料算会让齐套永远缺料（虚拟件库存永远是 0）；
 * 把 {@code BACKFLUSH} 计入齐套会让辅料没领就开不了工。
 */
public enum MesIssueMethodEnum {

    /** 领料：常规发料，计入齐套需求。 */
    PICK,
    /** 倒冲：报工时按实际产量反冲扣料，不计入齐套、不领料。 */
    BACKFLUSH,
    /** 虚拟件：BOM 的结构层次，物理上不存在，本身不产生需求，需继续向下展开子件。 */
    PHANTOM,
    /** 直送线边：供应商直接送到线边，仍按常规料计入齐套需求。 */
    DIRECT;

    /** 是否虚拟件：不产生需求，但要继续向下展开它的子件 BOM。 */
    public static boolean isPhantom(String issueMethod) {
        return ObjUtil.equal(PHANTOM.name(), issueMethod);
    }

    /** 是否倒冲件：既不产生需求也不展开子件，报工时反冲。 */
    public static boolean isBackflush(String issueMethod) {
        return ObjUtil.equal(BACKFLUSH.name(), issueMethod);
    }

}
