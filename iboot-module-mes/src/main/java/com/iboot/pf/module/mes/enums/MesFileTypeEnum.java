package com.iboot.pf.module.mes.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

/**
 * 附件文件类型。与字典 {@code mes_defect_file_type} 一致。
 *
 * <p>按扩展名判定统一放在这里：前后端两份映射表必然漂移，所以前端不传 fileType，
 * 由后端在写入附件时判定。
 */
@Getter
@AllArgsConstructor
public enum MesFileTypeEnum implements ArrayValuable<String> {

    /** 图片。 */
    IMAGE("图片"),
    /** 视频。 */
    VIDEO("视频"),
    /** PDF 文件。 */
    PDF("PDF文件"),
    /** 图纸。 */
    DRAWING("图纸"),
    /** Office 文档。 */
    OFFICE("Office文档"),
    /** 其他。 */
    OTHER("其他");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "avi", "mov", "wmv", "mkv", "flv", "webm");
    private static final Set<String> DRAWING_EXTENSIONS = Set.of("dwg", "dxf", "step", "stp", "igs", "iges");
    private static final Set<String> OFFICE_EXTENSIONS =
            Set.of("doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv");

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /**
     * 按文件名扩展名判定文件类型，未识别的扩展名兜底为 OTHER。
     *
     * @param fileName 原始文件名
     * @return 文件类型编码
     */
    public static String ofFileName(String fileName) {
        if (fileName == null) {
            return OTHER.name();
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return OTHER.name();
        }
        String extension = fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        if (IMAGE_EXTENSIONS.contains(extension)) {
            return IMAGE.name();
        }
        if (VIDEO_EXTENSIONS.contains(extension)) {
            return VIDEO.name();
        }
        if ("pdf".equals(extension)) {
            return PDF.name();
        }
        if (DRAWING_EXTENSIONS.contains(extension)) {
            return DRAWING.name();
        }
        if (OFFICE_EXTENSIONS.contains(extension)) {
            return OFFICE.name();
        }
        return OTHER.name();
    }

}
