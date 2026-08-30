package com.iboot.pf.module.aps.dal.dataobject.projection;


import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*; import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO; import lombok.*; import java.time.LocalDateTime;
/** APS 物料投影。 */
@TableName("aps_projection_mat") @Data @EqualsAndHashCode(callSuper = true) @Builder @NoArgsConstructor @AllArgsConstructor
public class ApsMaterialProjectionDO extends BaseDO {
    @TableId(type = IdType.ASSIGN_ID) private Long id; private Long scenarioId; private Long sourceMatId; private String matCode;
    private String matName; private String matSpec; private String matType; private Long baseUomId; private String baseUomCode;
    private Boolean batchFlag; private Long bomId; private String bomVersion; private Long routeId; private String routeVersion;
    private LocalDateTime sourceUpdateTime; private LocalDateTime snapshotTime; private String remark;
}
