package com.iboot.pf.module.mes.service.optask.gate;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 开工门禁的预检结果。
 *
 * <p>{@code check} 走"任一项失败就抛异常"的路子，适合 {@code start} 动作——不满足就不许开工。
 * 但作业页要回答的是另一个问题："这道工序现在为什么不能开工"，一次把所有不满足的项都列出来，
 * 车间才能一趟把料领齐、把工装准备好，而不是修一项再点一次开工试试。
 *
 * <p>所以预检不抛异常，把每一项的通过与否和原因都收进来。跳过的项也记录，
 * 否则车间会以为"这项通过了"，其实是这道工序压根不做这项校验。
 */
@Data
public class MesStartGateResult {

    /** 全部启用的校验项是否都通过。跳过的项不算失败。 */
    private boolean passed = true;

    /** 逐项结果，按执行顺序。 */
    private final List<Item> items = new ArrayList<>();

    /** 记一项通过。 */
    public void addPassed(String name) {
        items.add(new Item(name, true, false, null));
    }

    /** 记一项失败，并把整体结论置为不通过。 */
    public void addFailed(String name, String message) {
        items.add(new Item(name, false, false, message));
        passed = false;
    }

    /** 记一项被跳过（本工序不做这项校验），不影响整体结论。 */
    public void addSkipped(String name) {
        items.add(new Item(name, true, true, null));
    }

    /** 第一条失败原因，供只想给一句话提示的调用方使用。 */
    public String firstFailureMessage() {
        return items.stream().filter(item -> !item.passed).map(Item::getMessage).findFirst().orElse(null);
    }

    /** 逐项结果。 */
    @Data
    public static class Item {

        /** 校验项名称，取值见 {@link MesStartGateNames}。 */
        private final String name;

        /** 是否通过。跳过的项记为通过。 */
        private final boolean passed;

        /** 是否被跳过（本工序不启用这一项）。 */
        private final boolean skipped;

        /** 失败原因，通过或跳过时为空。 */
        private final String message;

    }

}
