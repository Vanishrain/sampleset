package cn.iecas.sampleset.datasource;

import cn.iecas.sampleset.dao.SampleInfoMapper;
import cn.iecas.sampleset.pojo.domain.SampleInfo;
import cn.iecas.sampleset.pojo.entity.Sample;
import cn.iecas.sampleset.utils.FastDFSUtil;
import org.csource.common.MyException;
import org.csource.fastdfs.StorageClient1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(prefix="value",name = "datasource", havingValue = "fastdfs")
public class FDFSSourceImpl implements BaseDataSource {
    @Autowired
    public SampleInfoMapper sampleInfoMapper;

    @Override
    public void close() {
    }


    /**
     * 初始化数据源连接
     */
    @Override
    public void initialize() {

    }


    /**
     * 根据数据集id删除全部切片数据
     * @param imageSetId
     */
    @Override
    public void deleteImageSetById(int imageSetId) {

    }

    /**
     * 根据切片名称获取数据集中的切片
     * @param visualPath
     * @return
     */
    @Override
    public Sample getImageByName(String visualPath) {
        Sample sample = new Sample();
        StorageClient1 storageClient = FastDFSUtil.getSrorageClient();
        byte[] result = null;
        try {
            result = storageClient.download_file1(visualPath);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        BASE64Encoder encoder = new BASE64Encoder();
        String imageDataString = encoder.encode(result).replaceAll("\r|\n", "");
        String base64Tile = "data:image/jpeg;base64," + imageDataString;
        sample.setSampleThumb(base64Tile);
        return sample;
    }


//    public static void printTile(byte[] tileBuffer,String path,int tileSize){
//        FileOutputStream fs;
//        try {
//            fs = new FileOutputStream(path);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            return ;
//        }
//        ByteArrayInputStream inputStream = new ByteArrayInputStream(tileBuffer);
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        BufferedImage image = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_RGB);
//        try {
//            image.getGraphics().drawImage(ImageIO.read(inputStream), 0, 0, tileSize, tileSize, null);
//            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(bos);
//            encoder.encode(image);
//            fs.write(bos.toByteArray());
//            fs.flush();
//            fs.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
    /**
     * 根据切片名称获取数据集中的切片
     * @param path
     * @return
     */
    @Override
    public String getImageByPath(String path) {
        StorageClient1 storageClient = FastDFSUtil.getSrorageClient();
        byte[] result = null;
        try {
            result = storageClient.download_file1(path);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        BASE64Encoder encoder = new BASE64Encoder();
        String imageDataString = encoder.encode(result).replaceAll("\r|\n", "");
        String base64Tile = "data:image/jpeg;base64," + imageDataString;

        return base64Tile;
    }

    @Override
    public void deletes(SampleInfo sampleInfo) throws Exception {
        String storagePath;
        String visualPath;
        String labelPath;

        if (sampleInfo != null){
            storagePath = sampleInfo.getStoragePath();
            visualPath = sampleInfo.getVisualPath();
            labelPath = sampleInfo.getLabelPath();

            if (storagePath != null){
                FastDFSUtil.delete(storagePath);
            }
            if (visualPath != null){
                FastDFSUtil.delete(visualPath);
            }
            if (labelPath != null){
                FastDFSUtil.delete(labelPath);
            }
        }else {
            throw new Exception("该切片不存在");
        }
    }

    @Override
    public byte[] download(String fileId) throws Exception {
        return FastDFSUtil.download(fileId);
    }

    /**
     * 根据切片名称删除数据集中的切片
     * @param imageSetId
     * @param imageName
     */
    @Override
    public void deleteImageByName(int imageSetId, String imageName) {

    }

    /**
     * 分页获取数据集切片
     * @param sampleInfos
     * @return
     */
    @Override
    public List<Sample> getImages(List<SampleInfo> sampleInfos) throws Exception {
        List<Sample> sampleList = new ArrayList<>();
        StorageClient1 storageClient = FastDFSUtil.getSrorageClient();
        BASE64Encoder encoder = new BASE64Encoder();

        for (SampleInfo sampleInfo : sampleInfos){
            String imageBase64String = null;
            if(sampleInfo.isHasThumb())
                imageBase64String = sampleInfo.getSampleThumb();
            else {
                String storagePath = sampleInfo.getStoragePath();//存储路径
                byte[] result = storageClient.download_file1(storagePath);
                if (result==null)
                    continue;
                String encodedimageData = encoder.encode(result);
                imageBase64String = ("data:image/jpeg;base64," + encodedimageData).replaceAll("\r|\n", "");
            }

            Sample sample = new Sample();
            sample.setId(sampleInfo.getId());
            sample.setSampleThumb(imageBase64String);
            sample.setName(sampleInfo.getName());
            sample.setCreateTime(sampleInfo.getCreateTime());
            sampleList.add(sample);

        }

        return sampleList;
    }

}
