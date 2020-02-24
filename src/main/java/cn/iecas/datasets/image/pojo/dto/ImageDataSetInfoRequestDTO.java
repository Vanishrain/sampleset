package cn.iecas.datasets.image.pojo.dto;

import lombok.Data;

import java.text.SimpleDateFormat;
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
//    private Date endTime;
    private String endTime;

    //时间范围
//    private Date beginTime;
    private String beginTime;

    private String attribute;

    public void setBeginTime(long beginTime){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.beginTime = simpleDateFormat.format(beginTime);
    }

    public void setEndTime(long endTime){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.endTime = simpleDateFormat.format(endTime);
    }
}
