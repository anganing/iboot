package com.iboot.pf.module.aps.service.shortage;
import com.iboot.pf.module.aps.api.shortage.dto.RefreshShortageReqDTO; import com.iboot.pf.module.aps.dal.dataobject.shortage.ApsShortageDO; import java.util.List;
/** APS 缺料服务。 */
public interface ApsShortageService { List<ApsShortageDO> getShortageListByVersionId(Long versionId); void refreshShortage(RefreshShortageReqDTO reqDTO); }
