package com.iboot.pf.module.mes.service;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import com.iboot.pf.module.mes.api.dto.MesRespDTO;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** MES API 对应的内部业务服务骨架。接入业务表后由领域 Service 替换实现。 */
public interface MesApiService {

    default Long create(String domain, MesCommandReqDTO req) { throw unsupported(domain); }
    default void command(String domain, MesCommandReqDTO req) { throw unsupported(domain); }
    default MesRespDTO get(String domain, Long id) { throw unsupported(domain); }
    default List<MesRespDTO> list(String domain, Collection<Long> ids) { return Collections.emptyList(); }
    default List<MesRespDTO> listBy(String domain, String key, Collection<Long> ids) { return Collections.emptyList(); }

    default MesRespDTO checkKit(MesCommandReqDTO req) { throw unsupported("kit"); }
    default void submitQuality(MesCommandReqDTO req) { throw unsupported("quality"); }
    default void createDisposition(MesCommandReqDTO req) { throw unsupported("disposition"); }

    private UnsupportedOperationException unsupported(String domain) {
        return new UnsupportedOperationException("MES " + domain + " 业务 Service 尚未接入领域表");
    }
}
