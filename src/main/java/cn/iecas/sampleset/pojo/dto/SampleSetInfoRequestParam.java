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
public class SampleSetInfoRequestParam {
    /**
     * 样本集id
     */
    private int id;

    /**
     * 样本集名称
     */
    private String name;

    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endTime;

    /**
     * 开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date beginTime;


    /**
     * 请求第几页
     */
    private int pageNo = 1;

    /**
     * 每页的数量
     */
    private int pageSize = 10;
}
