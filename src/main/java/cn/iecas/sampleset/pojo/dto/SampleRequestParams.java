package cn.iecas.sampleset.pojo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author vanishrain
 */
@Data
public class SampleRequestParams {
    /**
     * 起始页
     */
    private int pageNo = 1;

    /**
     * 页大小
     */
    private int pageSize = 10;

    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endTime;

    /*
     * 开始时间
     * */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date beginTime;

    /**
     * 样本集id
     */
    private int sampleSetId;

}
