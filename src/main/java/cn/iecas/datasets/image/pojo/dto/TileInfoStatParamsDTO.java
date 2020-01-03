package cn.iecas.datasets.image.pojo.dto;

import lombok.Data;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 根据时间段统计tileinfos数量的参数
 */
@Data
public class TileInfoStatParamsDTO {
    private String timeStep = "%Y-%m";
    private Date endTime = new Date();
    private List<String> imageSetIdList =null;
    private Date beginTime = new Date(1546272000000l);


    public void setTimeStep(String step){
        step = step.toLowerCase();
        switch (step){
            case "year": this.timeStep = "%Y"; break;
            case "month": this.timeStep = "%Y-%m"; break;
            case "day": this.timeStep = "%Y-%m-%d"; break;
            default:this.timeStep = "%Y-%m";break;

        }
    }
    public void setImageSetIdList(String imageSetIdList){
        this.imageSetIdList =Arrays.asList(imageSetIdList.split(","));
    }
    public void setBeginTime(long beginTime){
        this.beginTime = new Date(beginTime);
    }

    public void setEndTime (long endTime){ this.endTime = new Date(endTime); }
}
