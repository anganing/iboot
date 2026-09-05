package com.iboot.pf.module.wms.service.arrivalorder.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 修改到货登记的内部参数。
 *
 * <p>改单在语义上就是"按新内容整体替换"，字段集与创建时完全相同，所以直接继承创建参数、
 * 只补一个目标编号，避免两份字段各自漂移。
 *
 * <p>继承来的 {@code arrivalNo} 在改单时被忽略：单号是单据身份，不随内容改。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WmsUpdateArrivalBO extends WmsCreateArrivalBO {

    /** 到货登记编号。 */
    private Long arrivalId;

}
