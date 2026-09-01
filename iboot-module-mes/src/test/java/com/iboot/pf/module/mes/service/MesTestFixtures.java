package com.iboot.pf.module.mes.service;

import com.iboot.pf.module.mdm.api.bom.dto.BomRespDTO;
import com.iboot.pf.module.mdm.api.defectcode.dto.DefectCodeRespDTO;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mdm.api.op.dto.OpRespDTO;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpRespDTO;
import com.iboot.pf.module.mdm.api.route.dto.RouteRespDTO;
import com.iboot.pf.module.mes.enums.MesDefectLevelEnum;

/**
 * MES 单测共用的主数据桩对象。
 *
 * <p>mdm、wms、system 的数据一律通过门面获取，单测里把门面 mock 掉，这里只提供返回值，
 * 避免每个测试类重复拼 DTO。
 */
public final class MesTestFixtures {

    private MesTestFixtures() {
    }

    /** 物料快照桩，编码与名称用于断言快照是否落库。 */
    public static MaterialRespDTO material() {
        MaterialRespDTO material = new MaterialRespDTO();
        material.setId(1L);
        material.setCode("MAT-001");
        material.setName("定子铁芯");
        material.setSpec("120*80");
        return material;
    }

    /** 计量单位编码桩。 */
    public static String uomCode() {
        return "PCS";
    }

    /** BOM 桩，用于工单锁定版本。 */
    public static BomRespDTO bom() {
        BomRespDTO bom = new BomRespDTO();
        bom.setId(11L);
        bom.setVersion("B1.0");
        return bom;
    }

    /** 工艺路线桩，用于工单锁定版本。 */
    public static RouteRespDTO route() {
        RouteRespDTO route = new RouteRespDTO();
        route.setId(21L);
        route.setVersion("R1.0");
        return route;
    }

    /** 路线工序桩，顺序执行，无并行组。 */
    public static RouteOpRespDTO routeOp() {
        RouteOpRespDTO routeOp = new RouteOpRespDTO();
        routeOp.setId(31L);
        routeOp.setRouteId(21L);
        routeOp.setOpId(41L);
        routeOp.setSeq(10);
        routeOp.setKeyFlag(Boolean.TRUE);
        return routeOp;
    }

    /** 标准工序桩。 */
    public static OpRespDTO op() {
        OpRespDTO op = new OpRespDTO();
        op.setId(41L);
        op.setCode("OP010");
        op.setName("定子绕线");
        return op;
    }

    /** 缺陷代码桩，等级用于断言缺陷记录的等级快照。 */
    public static DefectCodeRespDTO defectCode() {
        DefectCodeRespDTO defectCode = new DefectCodeRespDTO();
        defectCode.setId(51L);
        defectCode.setCode("DEF-001");
        defectCode.setName("绕线松动");
        defectCode.setLevel(MesDefectLevelEnum.MAJOR.name());
        return defectCode;
    }

}
