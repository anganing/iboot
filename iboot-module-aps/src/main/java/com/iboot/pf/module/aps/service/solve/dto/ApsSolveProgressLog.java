package com.iboot.pf.module.aps.service.solve.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 单条求解进度日志。
 *
 * @author iboot
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApsSolveProgressLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 进度状态。 */
    private String status;

    /** 日志内容。 */
    private String content;

    /** 发生时间。 */
    private LocalDateTime occurTime;

}
