package cn.iecas.sampleset.utils;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 影像切片工具
 * @author vanishrain
 */
public class SliceGenerateUtil {
    /**
     * 根据原图像数据和切片xml对图像进行切割
     * 存在output路径中
     * @param pointList 像素/经纬度坐标点列表
     * @param type 坐标点类型 geodegree：经纬度 pixel：像素
     * @param srcPath 原始影像路径
     * @param outputPath 输出切片路径
     */
    public static void generateSlice(List<String> pointList, String type, String srcPath, String outputPath){
        gdal.AllRegister();
        double[] range = new double[4];
        double[] sliceGeoTransform = null;


        Dataset dataset = gdal.Open(srcPath);
        range[0] = pointList.stream().map(point->Double.parseDouble(point.split(",")[0])).min(Double::compareTo).get();
        range[1] = pointList.stream().map(point->Double.parseDouble(point.split(",")[1])).min(Double::compareTo).get();
        range[2] = pointList.stream().map(point->Double.parseDouble(point.split(",")[0])).max(Double::compareTo).get();
        range[3] = pointList.stream().map(point->Double.parseDouble(point.split(",")[1])).max(Double::compareTo).get();

        if ("geodegree".equals(type.toLowerCase())){
            double[] srcGeoTransform = dataset.GetGeoTransform();
            sliceGeoTransform = new double[]{range[0],srcGeoTransform[1],0,range[1],0,srcGeoTransform[5]};
            range = convertGeoDegreeToPixel(range, dataset);
        }

        Driver pDriver = gdal.GetDriverByName("GTiff");
        int minX = (int)range[0], minY = (int)range[1], maxX = (int)range[2], maxY = (int)range[3];
        int sliceWidth = maxX - minX, sliceHeight = maxY - minY;
        File outputFile = new File(outputPath);
        if(!outputFile.getParentFile().exists())
            outputFile.getParentFile().mkdirs();

        Dataset pDSOut = pDriver.Create(outputPath, sliceWidth, sliceHeight, dataset.getRasterCount(), gdalconst.GDT_Byte);
        byte[] out = new byte[sliceWidth*sliceHeight*dataset.getRasterCount()];
        pDSOut.SetProjection(dataset.GetProjection());
        if (sliceGeoTransform !=null) pDSOut.SetGeoTransform(sliceGeoTransform);

        for (int band = 1; band <= dataset.getRasterCount(); band++) {
            Band pBandRead = dataset.GetRasterBand(band);
            Band pBandWrite = pDSOut.GetRasterBand(band);
            pBandRead.ReadRaster(minX,minY,sliceWidth,sliceHeight,sliceWidth,sliceHeight,
                    gdalconst.GDT_Byte,out);
            pBandWrite.WriteRaster(0,0,sliceWidth,sliceHeight,sliceWidth,sliceHeight,
                    gdalconst.GDT_Byte,out);
            pBandRead.delete();
            pBandWrite.delete();
        }

        dataset.delete();
        pDSOut.delete();
    }

    /**
     * 获取原始图像的经纬度范围
     * @param dataset 原始数据
     * @return 经纬度范围
     */
    public static double[] getBBoxFromImage(Dataset dataset){
        double[] range = new double[4];
        double argout[] = new double[3];
        double argout1[] = new double[3];
        int width = dataset.getRasterXSize();
        int hight = dataset.getRasterYSize();
        String projInfo = dataset.GetProjectionRef();
        double GeoTransform[] = dataset.GetGeoTransform();
        double x2 = GeoTransform[0] + width*GeoTransform[1];
        double y2 = GeoTransform[3] + hight*GeoTransform[5];

        SpatialReference oProject = new SpatialReference(projInfo);
        SpatialReference oLatLong;
        oLatLong = oProject.CloneGeogCS();
        CoordinateTransformation ct = new CoordinateTransformation(oProject, oLatLong);
        ct.TransformPoint(argout, GeoTransform[0], GeoTransform[3]);
        ct.TransformPoint(argout1, x2, y2);

        if(projInfo.equals("")){
            range[0] = GeoTransform[0];
            range[1] = y2;
            range[2] = x2;
            range[3] = GeoTransform[3];
        }else {
            //四角点经纬度
            range[0] = argout[0];
            range[1] = argout1[1];
            range[2] = argout1[0];
            range[3] = argout[1];
        }

        return range;
    }

    /**
     * 将经纬度范围转换为像素范围
     * @param pointList 经纬度坐标点
     * @param dataset 原始影像
     * @return 切片的像素范围
     */
    private static double[] convertGeoDegreeToPixel(double[] range, Dataset dataset){
        int srcWidth = dataset.getRasterXSize();
        int srcHeight = dataset.getRasterYSize();
        double[] srcInfo = getBBoxFromImage(dataset);

        double srcLatRange = srcInfo[3] - srcInfo[1];
        double srcLonRange = srcInfo[2] - srcInfo[0];
        int minX = (int) (srcWidth * (range[0] - srcInfo[0]) / srcLonRange);
        int maxX = (int) (srcWidth * (range[2] - srcInfo[0]) / srcLonRange);
        int maxY = srcHeight - (int) (srcHeight * (range[1] - srcInfo[1]) / srcLatRange);
        int minY = srcHeight - (int) (srcHeight * (range[3] - srcInfo[1]) / srcLatRange);

        double[] pixel = new double[4];
        pixel[0] = minX;
        pixel[1] = minY;
        pixel[2] = maxX;
        pixel[3] = maxY;
        return pixel;
    }

    public static void main(String[] args) {
        List<String> points = new ArrayList<>();
        points.add("144.9138840253173,13.576115980830716");
        points.add("144.9138840253173,13.601717888818072");
        points.add("144.94491515807974,13.601717888818072");
        points.add("144.94491515807974,13.576115980830716");
        generateSlice(points,"geodegree","D:\\temp\\安德森空军基地_2006-02-28.tif","d:\\temp\\test.jpg");
    }
}
