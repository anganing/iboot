package com.iboot.pf.module.mdm.controller.admin.uom.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UomRespVO {
    private Long id;
    private String code;
    private String name;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
}
