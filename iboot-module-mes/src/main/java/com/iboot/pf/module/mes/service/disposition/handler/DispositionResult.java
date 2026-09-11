package com.iboot.pf.module.mes.service.disposition.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 处置联动产生的可追溯业务凭据。 */
@Getter
@AllArgsConstructor
public class DispositionResult {

    private final String targetBizType;
    private final String targetBizNo;
}
