package cn.iecas.datasets.image.pojo.domain;

import cn.iecas.datasets.image.common.DateUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.Generated;

import java.text.ParseException;
import java.util.Date;

@Data
@TableName(value = "image_infos")
public class ImageDataSetInfoDO {

    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    private String path;
    private String description;
    private String attribute;
    private String datasource;
    private Integer bit;
    private Integer band;
    private Double datasize;
    private Float definition;
    private String size;
    private Integer number;
    private String postfix;
    private Float labelsize;
    private String labeldescription;
    private String labelpostfix;
    private String direction;
    private String construction;
    private Date datatime;
    private String name;
    private String category;
    private long targetnum;


    public void setDatatime(String time)  {
        try {
            this.datatime=DateUtil.fromStringToDate(time,"yyyy-MM-dd");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
