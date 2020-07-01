package cn.iecas.sampleset.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * 样本集创建api参数
 * @author vanishrain
 */
@Data
public class SampleSetCreationInfo {
    /**
     * 用户id
     */
    private int userId;


    /**
     * 数据来源
     */
    private String source;

    /**
     * 关键字
     */
    private String keywords;

    /**
     * 是否制作切片
     */
    private boolean isSlice;


    /**
     * 数否公开
     */
    private boolean isPublic;


    /**
     * 样本集描述
     */
    private String description;


    /**
     * 数据集manifest文件路径
     */
    private String manifestPath;

    /**
     * 样本集名称
     */
    private String sampleSetName;


}
