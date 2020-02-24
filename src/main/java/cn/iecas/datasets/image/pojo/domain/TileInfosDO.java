package cn.iecas.datasets.image.pojo.domain;


import cn.iecas.datasets.image.utils.DateUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@TableName(value = "tile_infos")
public class TileInfosDO {
    @TableId(type = IdType.AUTO,value = "id")
    private int id;

    private int band;

    private int taskid;

    private int version;

    private String name;

    private int targetNum;

    private int imagesetid;


    private Date createTime;

    private String labelPath;

    private String visualPath;

    private Double resolution;


    private String storagePath;

    public void setCreateTime(Date time){
        this.createTime = time;
    }


    public void setCreateTime(String time){
        try {
            this.createTime = DateUtil.fromStringToDate(time,"yyyy-MM-dd");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void setCreateTime(){
        try {
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            this.createTime = DateUtil.fromStringToDate(sdf.format(date), "yyyy-MM-dd HH:mm:ss");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
