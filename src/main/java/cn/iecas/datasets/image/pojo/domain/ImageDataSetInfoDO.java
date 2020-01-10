package cn.iecas.datasets.image.pojo.domain;

import cn.iecas.datasets.image.common.DateUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.text.ParseException;
import java.util.Date;

@Data
@TableName(value = "image_infos")
public class ImageDataSetInfoDO {

    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    private String name;
    private String path;

    @TableField(value = "sensortype")
    private String sensorType;
    @TableField(value = "datasource")
    private String dataSource;
    @TableField(value = "datatime")
    private Date dataTime;
    private String category;
    private String config;
    private float resolution;

    @TableField(value = "band")
    private int bands;

    private int bit;

    @TableField(value = "datasize")
    private double dataSize;

    private String description;

    @TableField(value = "targetnum")
    private long targetNum;
    private long number;


    public void setDatatime(String time)  {
        try {
            this.dataTime=DateUtil.fromStringToDate(time,"yyyy-MM-dd");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
