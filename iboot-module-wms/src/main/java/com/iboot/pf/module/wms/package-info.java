/**
 * wms 包下，仓储管理系统（Warehouse Management System）。
 * 例如说：入库、出库、库存、盘点、库位等等
 *
 * 1. Controller URL：以 /wms/ 开头，避免和其它 Module 冲突
 * 2. DataObject 表名：以 wms_ 开头，方便在数据库中区分
 *
 * 注意，由于 Wms 模块下容易和其它模块重名，所以类名都加 Wms 前缀~
 *
 * <h2>两套 Service 的分工边界</h2>
 *
 * 本模块是"先 codegen 生成 CRUD、再补领域层"的产物，同一张表下存在两个 Service。分工是固定的：
 *
 * <ul>
 *   <li><b>领域 Service（{@code Wms*Service}）承担全部写入</b>。库存余额只能经
 *       {@code WmsInventoryService#post} 改；单据状态只能由 {@code submit} / {@code confirm} /
 *       {@code cancel} 这类领域动作推进，每个动作都带幂等键并留下 {@code wms_inv_txn} 流水。</li>
 *   <li><b>codegen Service（无前缀的 {@code XxxService}）只做列表展示查询</b>。它的
 *       {@code getXxx} / {@code getXxxPage} / {@code getXxxList} 与 Mapper XML 里的 JOIN
 *       查询有展示价值（能一次带出物料编码、仓库名称这类可读字段），所以保留；写入型方法
 *       （{@code createXxx} / {@code updateXxx} / {@code deleteXxx} / {@code importXxxList}）
 *       已删除。</li>
 * </ul>
 *
 * 为什么必须这样切：CRUD 界面直接改余额或单据状态，等于绕开状态机与流水台账——界面上做的每笔
 * 出入库都不改账、账实永久对不上，而且没有任何痕迹可追。
 *
 * <h3>三处有意保留的例外</h3>
 *
 * 以下 codegen 写入方法是**唯一写入实现**，删了就没有对应能力，故保留：
 *
 * <ol>
 *   <li>{@code wh} / {@code zone} / {@code loc} / {@code containertype} / {@code containermgr}
 *       五张 A 类配置主数据：编码生成、编码唯一校验、级联占用校验都在 codegen 侧，领域侧的
 *       {@code WmsWarehouseService} 只有读方法与校验方法。</li>
 *   <li>{@code batchmgr} 的 {@code BatchService#updateBatch}：给批属性维护
 *       （{@code PUT /wms/batch/update-attr}）用。{@code wms_batch} 是主档表不是账表——账表只有
 *       {@code wms_inv_balance} / {@code wms_inv_txn} / {@code wms_inv_reservation} /
 *       {@code wms_inv_lock} 四张——炉批号、生产日期、失效日期这些是"注册之后才知道"的主数据补录，
 *       不改任何数量，与"账只能由领域动作改"不冲突。可改字段已由
 *       {@code BatchAttrUpdateReqVO} 锁死在六个。</li>
 *   <li>{@code strategy} 仓储策略：配置类，本就只有一套实现。</li>
 * </ol>
 *
 * <h3>单据明细行没有行级写入入口</h3>
 *
 * 五类单据（入库、出库、转移、盘点、到货）的明细行<b>不提供</b>行级增删改端点。明细是单据的一部分，
 * {@code DRAFT} 态的编辑走主单 {@code updateXxx}（领域侧 {@code insertLines} 是"旧行整批删除后
 * 重建"语义），非 {@code DRAFT} 态的明细不允许改——那是流水已经记过的数据。行级 CRUD 既绕开主单
 * 状态校验，也维护不了 {@code line_no} 连续性与单头汇总。
 *
 * @see com.iboot.pf.module.wms.service.inventory.WmsInventoryService 库存余额的唯一写入出口
 * @see com.iboot.pf.module.wms.service.command.WmsCommandService 跨模块命令幂等载体
 */
package com.iboot.pf.module.wms;
