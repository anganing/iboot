package com.iboot.pf.module.wms.dal.dataobject.command;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * WMS 跨模块命令幂等记录。
 *
 * <p>只承载没有落库载体的状态推进型命令（单据提交、确认、取消这类）。已经自带
 * {@code idempotency_key} 列的表（{@code wms_inv_txn}、{@code wms_in_order} 等）
 * 继续用列上的唯一索引判重，不重复记一份。
 */
@TableName("wms_command_record")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsCommandRecordDO extends BaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /** 幂等键，格式 sourceModule:sourceBizNo:action:sourceBizLineNo。 */
    private String idempotencyKey;
    /** 命令类型，取值见 WmsCommandTypeEnum。 */
    private String commandType;
    /** 来源模块，取值见 WmsSourceModuleEnum。 */
    private String sourceModule;
    /** 来源业务单号。 */
    private String sourceBizNo;
    /** 来源业务行号，整单命令为空。 */
    private String sourceBizLineNo;
    /** 首次执行的结果编号，无返回值的命令为空。 */
    private Long resultId;

}
