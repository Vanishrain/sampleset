package cn.iecas.sampleset.pojo.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Image implements Serializable {
    private static final long serialVersionUID = 8551305852347977301L;
    private int id;
    private int bands;
    private int width;
    private int height;
    private int userId;
    private String bit;
    private String path;
    private String thumb;
    private String source;
    private double minLat;
    private double minLon;
    private double maxLat;
    private double maxLon;
    private Date createTime;
    private int batchNumber;
    private String imageName;
    private boolean isPublic;
    private String projection;
}
