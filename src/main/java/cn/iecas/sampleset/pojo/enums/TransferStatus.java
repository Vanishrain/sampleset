package cn.iecas.sampleset.pojo.enums;


import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 结果类型枚举
 */
@JsonFormat(shape = JsonFormat.Shape.NUMBER)
public enum TransferStatus {
    TRANSFERING,DECOMPRESSING,STORAGING,FINISHED;
}
