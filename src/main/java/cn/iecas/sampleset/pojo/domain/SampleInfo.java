package cn.iecas.sampleset.pojo.domain;


import cn.iecas.sampleset.utils.DateUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sample_info")
public class SampleInfo {
    @TableId(type = IdType.AUTO,value = "id")
    private int id;

    private int bands;

    private String bit;

    private int taskId;

    private int version;

    private String name;

    private int targetNum;

    private int sampleSetId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private boolean hasThumb;

    private String labelPath;

    private String visualPath;

    private Double resolution;

    private String storagePath;

    private String visualThumb;

    private String sampleThumb;



//    public void setCreateTime(Date time){
//        this.createTime = time;
//    }
//

//    public void setCreateTime(String time){
//        try {
//            this.createTime = DateUtil.fromStringToDate(time,"yyyy-MM-dd");
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void setCreateTime(){
//        try {
//            Date date = new Date();
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            this.createTime = DateUtil.fromStringToDate(sdf.format(date), "yyyy-MM-dd HH:mm:ss");
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//    }
}
