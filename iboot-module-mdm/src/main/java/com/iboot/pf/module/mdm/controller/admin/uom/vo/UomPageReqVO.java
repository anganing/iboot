package com.iboot.pf.module.mdm.controller.admin.uom.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import lombok.Data;

@Data
public class UomPageReqVO extends PageParam {
    private String code;
    private String name;
    private Integer status;
}
