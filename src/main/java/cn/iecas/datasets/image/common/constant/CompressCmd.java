package cn.iecas.datasets.image.common.constant;

import lombok.Data;

/**
 * 分别定义rar，zip和tar三种解压缩格式及其list压缩包内容和解压缩命令
 */
public class CompressCmd {
    public static final String RAR_POSTFIX = "rar";

    public static final String ZIP_POSTFIX = "zip";

    public static final String TAR_POSTFIX = "tar";

    public static final String RAR_LIST_CMD = "unrar lb";

    public static final String RAR_DECOMPRESS_CMD = "unrar -o+ x";

    public static final String ZIP_LIST_CMD = "unzip -l";

    public static final String ZIP_DECOMPRESS_CMD = "unzip -q -o";

    public static final String TAR_LIST_CMD = "tar -tf";

    public static final String TAR_DECOMPRESS_CMD = "tar -x  -C";

    public static final String ZIP_COMPRESS_CMD = "zip %s -r %s";
}
