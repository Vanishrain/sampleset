package cn.iecas.sampleset.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author vanishrain
 */
@Data
public class Sample {
    private int id;
    private String name;
    private String sampleThumb;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Override
    public String toString() {
        String base64TileCopy = null == sampleThumb ? null : "normal value";
        return "Tile{" +
                "name='" + name + '\'' +
                ", createTime=" + createTime +
                ", id=" + id +
                ", base64Tile='" + base64TileCopy + '\'' +
                '}';
    }
}
