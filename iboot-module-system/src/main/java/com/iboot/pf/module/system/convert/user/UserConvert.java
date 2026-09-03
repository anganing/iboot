package com.iboot.pf.module.system.convert.user;

import com.iboot.pf.framework.common.util.collection.CollectionUtils;
import com.iboot.pf.framework.common.util.collection.MapUtils;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.system.controller.admin.dept.vo.dept.DeptSimpleRespVO;
import com.iboot.pf.module.system.controller.admin.dept.vo.post.PostSimpleRespVO;
import com.iboot.pf.module.system.controller.admin.permission.vo.role.RoleSimpleRespVO;
import com.iboot.pf.module.system.controller.admin.user.vo.profile.UserProfileRespVO;
import com.iboot.pf.module.system.controller.admin.user.vo.user.UserRespVO;
import com.iboot.pf.module.system.controller.admin.user.vo.user.UserSimpleRespVO;
import com.iboot.pf.module.system.dal.dataobject.dept.DeptDO;
import com.iboot.pf.module.system.dal.dataobject.dept.PostDO;
import com.iboot.pf.module.system.dal.dataobject.permission.RoleDO;
import com.iboot.pf.module.system.dal.dataobject.user.AdminUserDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

@Mapper
public interface UserConvert {

    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    default List<UserRespVO> convertList(List<AdminUserDO> list, Map<Long, DeptDO> deptMap,
                                        UnaryOperator<String> avatarSigner) {
        return CollectionUtils.convertList(list, user -> convert(user, deptMap.get(user.getDeptId()), avatarSigner));
    }

    default UserRespVO convert(AdminUserDO user, DeptDO dept, UnaryOperator<String> avatarSigner) {
        UserRespVO userVO = BeanUtils.toBean(user, UserRespVO.class);
        if (dept != null) {
            userVO.setDeptName(dept.getName());
        }
        userVO.setAvatar(avatarSigner.apply(userVO.getAvatar()));
        return userVO;
    }

    default List<UserSimpleRespVO> convertSimpleList(List<AdminUserDO> list, Map<Long, DeptDO> deptMap,
                                                    UnaryOperator<String> avatarSigner) {
        return CollectionUtils.convertList(list, user -> {
            UserSimpleRespVO userVO = BeanUtils.toBean(user, UserSimpleRespVO.class);
            MapUtils.findAndThen(deptMap, user.getDeptId(), dept -> userVO.setDeptName(dept.getName()));
            userVO.setAvatar(avatarSigner.apply(userVO.getAvatar()));
            return userVO;
        });
    }

    default UserProfileRespVO convert(AdminUserDO user, List<RoleDO> userRoles,
                                      DeptDO dept, List<PostDO> posts,
                                      UnaryOperator<String> avatarSigner) {
        UserProfileRespVO userVO = BeanUtils.toBean(user, UserProfileRespVO.class);
        userVO.setRoles(BeanUtils.toBean(userRoles, RoleSimpleRespVO.class));
        userVO.setDept(BeanUtils.toBean(dept, DeptSimpleRespVO.class));
        userVO.setPosts(BeanUtils.toBean(posts, PostSimpleRespVO.class));
        userVO.setAvatar(avatarSigner.apply(userVO.getAvatar()));
        return userVO;
    }

}
