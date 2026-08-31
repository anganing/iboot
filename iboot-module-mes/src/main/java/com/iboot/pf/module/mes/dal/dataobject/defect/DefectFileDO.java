package com.iboot.pf.module.mes.dal.dataobject.defect;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * MES-缺陷记录附件 DO：一条缺陷可挂多张照片、图纸或报告。
 *
 * @author iboot
 */
@TableName("mes_defect_file")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefectFileDO extends BaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 缺陷记录编号
     */
    private Long defectId;
    /**
     * 附件展示顺序，由后端按入参下标生成
     */
    private Integer seq;
    /**
     * 文件类型，由后端按扩展名判定
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesFileTypeEnum}
     */
    private String fileType;
    /**
     * 原始文件名，取自上传时的 File.name
     */
    private String fileName;
    /**
     * 文件地址，落库存去签名地址，读取时重新签发
     */
    private String fileUrl;
    /**
     * infra 文件编号
     */
    private Long fileId;

}
