package com.iboot.pf.module.aps.dal.dataobject.load;


import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.*; import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO; import lombok.*; import java.math.BigDecimal; import java.time.LocalDate;
/** APS 资源负荷。 */
@TableName("aps_load") @Data @EqualsAndHashCode(callSuper = true) @Builder @NoArgsConstructor @AllArgsConstructor
public class ApsLoadDO extends BaseDO {
    @TableId(type = IdType.ASSIGN_ID) private Long id; private Long versionId; private Long resId; private String resCode;
    private String resName; private LocalDate workDate; private Integer loadMinutes; private Integer capacityMinutes;
    private BigDecimal loadRate; private Integer overloadMinutes; private String remark;
}
