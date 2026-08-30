package com.iboot.pf.module.wms.service.batch.bo;

import lombok.Data;

import java.time.LocalDate;

/** 注册批次的内部参数。 */
@Data
public class RegisterBatchBO {

    /** 物料编号。 */
    private Long matId;

    /** 物料编码快照。 */
    private String matCode;

    /** 物料名称快照。 */
    private String matName;

    /** 批次号；无批次物料传默认批次号。 */
    private String lotNo;

    /** 序列号，可为空。 */
    private String snNo;

    /** 供应商编号，可为空。 */
    private Long supplierId;

    /** 供应商编码快照，可为空。 */
    private String supplierCode;

    /** 供应商批次号，可为空。 */
    private String supplierLotNo;

    /** 炉批号，可为空。 */
    private String furnaceLotNo;

    /** 生产日期，可为空。 */
    private LocalDate produceDate;

    /** 失效日期，可为空。 */
    private LocalDate expiryDate;

}
