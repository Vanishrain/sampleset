package cn.iecas.datasets.image.pojo.dto;

import lombok.Data;

import java.text.SimpleDateFormat;

/**
 * @author vanishrain
 */
@Data
public class TileRequestDTO {
    private int pageNo = 1;

    private int pageSize = 10;

    private int imageDatasetId ;

    /*
    * 时间范围
    * */
    private String beginTime;
    private String endTime;

    public void setBeginTime(long beginTime){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.beginTime = simpleDateFormat.format(beginTime);
    }

    public void setEndTime(long endTime){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.endTime = simpleDateFormat.format(endTime);
    }
}
