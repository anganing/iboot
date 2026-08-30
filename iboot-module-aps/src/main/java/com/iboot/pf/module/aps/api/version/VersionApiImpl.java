package com.iboot.pf.module.aps.api.version;
import com.iboot.pf.framework.common.util.object.BeanUtils; import com.iboot.pf.framework.datapermission.core.annotation.DataPermission; import com.iboot.pf.module.aps.api.version.dto.*; import com.iboot.pf.module.aps.service.version.ApsVersionService; import jakarta.annotation.Resource; import org.springframework.stereotype.Service; import org.springframework.validation.annotation.Validated; import java.util.*;
/** 排程版本 API 实现。 */ @Service @Validated
public class VersionApiImpl implements VersionApi { @Resource private ApsVersionService service;
 public Long generateVersion(GenerateVersionReqDTO r){return service.generateVersion(r);} @DataPermission(enable=false) public VersionRespDTO getVersion(Long id){return BeanUtils.toBean(service.getVersion(id),VersionRespDTO.class);}
 @DataPermission(enable=false) public List<VersionRespDTO> getVersionListByScenarioId(Long id){return BeanUtils.toBean(service.getVersionListByScenarioId(id),VersionRespDTO.class);}
 public void confirmVersion(ChangeVersionStatusReqDTO r){service.confirmVersion(r);} public void publishVersion(ChangeVersionStatusReqDTO r){service.publishVersion(r);} public void replaceVersion(ReplaceVersionReqDTO r){service.replaceVersion(r);} }
