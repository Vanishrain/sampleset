package cn.iecas.datasets.image.pojo.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author vanishrain
 */
@Data
public class Tile {
    private String name;
    private Date createTime;
    private int id;
    private String base64Tile;

    @Override
    public String toString() {
        String base64TileCopy = null == base64Tile ? null : "normal value";
        return "Tile{" +
                "name='" + name + '\'' +
                ", createTime=" + createTime +
                ", id=" + id +
                ", base64Tile='" + base64TileCopy + '\'' +
                '}';
    }
}
