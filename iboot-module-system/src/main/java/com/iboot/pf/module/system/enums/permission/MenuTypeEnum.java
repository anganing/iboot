package com.iboot.pf.module.system.enums.permission;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 菜单类型枚举类
 *
 * @author iboot
 */
@Getter
@AllArgsConstructor
public enum MenuTypeEnum implements ArrayValuable<Integer> {

    DIR(1), // 目录
    MENU(2), // 菜单
    BUTTON(3) // 按钮
    ;

    /**
     * 类型
     */
    private final Integer type;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MenuTypeEnum::getType).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
