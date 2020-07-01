package cn.iecas.sampleset.pojo.domain;

import cn.iecas.sampleset.pojo.enums.SampleSetStatus;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sample_set_info")
public class SampleSetInfo {

    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    /**
     * 样本集包含的样本数量
     */
    private int count;

    /**
     * 用户id
     */
    private int userId;

    /**
     * 样本集名称
     */
    private String name;

    /**
     * 样本集存储路径
     */
    private String path;

    /**
     * 上传版本
     */
    private int version;

    /**
     * 样本集来源
     */
    private String source;

    /**
     * 关键字
     */
    private String keywords;

    /**
     * 样本集创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 是否公开
     */
    private boolean isPublic;

    /**
     * 样本集描述
     */
    private String description;

    /**
     * 数据集状态
     */
    private SampleSetStatus status = SampleSetStatus.FINISH;



}
