package cn.iecas.sampleset.datasource;

import cn.iecas.sampleset.pojo.domain.SampleInfo;
import cn.iecas.sampleset.pojo.entity.Sample;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(prefix="value",name = "datasource", havingValue = "lizardfs")
public class LizardfsSource implements BaseDataSource {

    @Value("${value.dir.rootDir}")
    private String rootDir;

    @Override
    public void close() {

    }

    @Override
    public void initialize() {

    }

    @Override
    public void deleteImageSetById(int imageSetId) {

    }

    @Override
    public Sample getImageByName(String imageName) {
        return null;
    }

    @Override
    public String getImageByPath(String path) {
        byte[] data = readFile(path);
        String base64Tile = null;

        if (data!=null){
            BASE64Encoder encoder = new BASE64Encoder();
            String imageDataString = encoder.encode(data).replaceAll("\r|\n", "");
            base64Tile = "data:image/jpeg;base64," + imageDataString;
        }
        return base64Tile;

    }

    @Override
    public void deletes(SampleInfo sampleInfo) throws Exception {
        File file = new File(rootDir + File.separator + sampleInfo.getStoragePath());
        if (file.exists())
            FileUtils.forceDelete(file);

        file = new File(rootDir + File.separator + sampleInfo.getVisualPath());
        if (file.exists())
            FileUtils.forceDelete(file);

        file = new File(rootDir + File.separator + sampleInfo.getLabelPath());
        if (file.exists())
            FileUtils.forceDelete(file);

    }

    @Override
    public byte[] download(String fileId) {
        String filePath = rootDir + File.separator + fileId;
        return readFile(filePath);
    }

    @Override
    public void deleteImageByName(int imageSetId, String imageName) {

    }

    @Override
    public List<Sample> getImages(List<SampleInfo> sampleInfos) {
        List<Sample> sampleList = new ArrayList<>();
        BASE64Encoder encoder = new BASE64Encoder();

        for (SampleInfo sampleInfo : sampleInfos){
            Sample sample = new Sample();
            BeanUtils.copyProperties(sampleInfo,sample);

            if (!sampleInfo.isHasThumb()){
                String storagePath = rootDir + File.separator + sampleInfo.getStoragePath();//存储路径
                byte[] data = readFile(storagePath);
                if (data==null)
                    continue;
                String thumb = ("data:image/jpeg;base64," + encoder.encode(data)).replaceAll("\r|\n", "");
                sample.setSampleThumb(thumb);
            }
            sampleList.add(sample);
        }

        return sampleList;
    }


    private byte[] readFile(String path){
        byte[] data = null;
        FileChannel fileChannel = null;
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(path,"r")){
            fileChannel = randomAccessFile.getChannel();
            MappedByteBuffer byteBuffer = fileChannel
                    .map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size()).load();
            data = new byte[(int)fileChannel.size()];
            if (byteBuffer.remaining()>0)
                byteBuffer.get(data,0,byteBuffer.remaining());
            fileChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (fileChannel!=null){
                try {
                    fileChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return data;
    }
}
