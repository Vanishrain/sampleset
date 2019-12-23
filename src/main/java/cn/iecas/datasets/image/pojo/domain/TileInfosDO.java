package cn.iecas.datasets.image.pojo.domain;


import cn.iecas.datasets.image.common.DateUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.text.ParseException;
import java.util.Date;

@Data
@TableName(value = "tile_infos")
public class TileInfosDO {
    @TableId(type = IdType.AUTO,value = "id")
    Integer id;
    Integer taskid;
    String dataPath;
    String labelPath;
    String visualPath;
    Double resolution;
    String sensor;
    Integer band;
    Integer imagesetid;
    Integer targetNum;
    Date createTime;
    String storagePath;

    public void setCreateTime(String time){
        try {
            this.createTime=DateUtil.fromStringToDate(time,"yyyy-MM-dd HH:mm:ss");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
