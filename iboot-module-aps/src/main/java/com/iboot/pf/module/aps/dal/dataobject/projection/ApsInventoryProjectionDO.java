package com.iboot.pf.module.aps.dal.dataobject.projection;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*; import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO; import lombok.*; import java.math.BigDecimal; import java.time.LocalDateTime;
/** APS 库存投影。 */
@TableName("aps_projection_inv") @Data @EqualsAndHashCode(callSuper = true) @Builder @NoArgsConstructor @AllArgsConstructor
public class ApsInventoryProjectionDO extends BaseDO {
    @TableId(type = IdType.ASSIGN_ID) private Long id; private Long scenarioId; private Long sourceBalanceId; private Long invOrgId;
    private Long plantId; private Long whId; private Long locId; private Long matId; private String matCode; private Long batchId;
    private String lotNo; private Long projectId; private Long projectTaskId; private String stockScope; private String qualityStatus;
    private BigDecimal qtyOnhand; private BigDecimal qtyReserved; private BigDecimal qtyFrozen; private BigDecimal qtyAvailable;
    private LocalDateTime sourceUpdateTime; private LocalDateTime snapshotTime; private String remark;
}
