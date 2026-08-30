package com.iboot.pf.module.aps.dal.dataobject.shortage;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*; import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO; import lombok.*; import java.math.BigDecimal; import java.time.LocalDateTime;
/** APS 缺料记录。 */
@TableName("aps_shortage") @Data @EqualsAndHashCode(callSuper = true) @Builder @NoArgsConstructor @AllArgsConstructor
public class ApsShortageDO extends BaseDO {
    @TableId(type = IdType.ASSIGN_ID) private Long id; private Long versionId; private Long prodOrderId; private Long opTaskId;
    private Long projectId; private Long projectTaskId; private Long matId; private String matCode; private String matName;
    private BigDecimal reqQty; private BigDecimal availQty; private BigDecimal shortQty; private Long uomId; private String uomCode;
    private LocalDateTime needDate; private LocalDateTime expectedSupplyDate; private String status; private String remark;
}
