package com.iboot.pf.module.mes.service.qcresult.bo;

import lombok.Data;

import java.util.List;

/**
 * 检验结果批量录入参数。
 *
 * <p>检验员的录入习惯是"一次把当前任务的格子填完再保存"，所以载体是任务 + 行列表，
 * 而不是单行重复调用——行级逐条提交既打不通"录完即判"的批量节奏，也没法在最后一行
 * 保存时统一判断"全部必检项是否录完"来推进任务。
 *
 * <p>**不走命令幂等**：结果录入是可重复覆盖的，同一格改数值重交是常规操作（录错了改数、
 * 补录漏掉的项），命令幂等会把第二次提交当成重复下发直接拦掉，改数就改不动了。
 * 去重兜底放在行列表内部：同一格（检验项 × 样本）在一批里出现两次按脏数据拒掉。
 */
@Data
public class MesSaveQcResultBO {

    /** 质检任务编号，必填。 */
    private Long taskId;

    /**
     * 检验员编号，必填。检验结果的落款：谁录的数、什么时候录的，事后追溯靠这两列。
     * 由调用方传登录人（UI）或指定身份（跨模块门面），领域服务不取登录上下文。
     */
    private Long inspectorId;

    /** 录入的实测值行，必填非空。 */
    private List<Item> results;

    /**
     * 单格录入行：检验项 × 样本序号定位一个格子，实测值是格子里唯一的新数据。
     */
    @Data
    public static class Item {

        /** 检验项目编号。 */
        private Long itemId;

        /** 样本序号，从 1 开始。 */
        private Integer sampleNo;

        /**
         * 实测值，必填。数值型直接录数字（如 {@code 0.056}），文本型录观察结果，
         * 布尔型录 {@code true}/{@code false} 或 {@code 1}/{@code 0}。
         */
        private String actualValue;

    }

}
