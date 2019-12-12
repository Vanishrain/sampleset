package cn.iecas.datasets.image.pojo.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author vanishrain
 */
@Data
public class ImageDataSetInfoRequestDTO {
    private int id;

    //请求第几页
    private int pageNo;

    //每页的数量
    private int pageSize;

    private String name;

    //时间范围
    private Date endTime;

    //时间范围
    private Date beginTime;


    private String attribute;
}
