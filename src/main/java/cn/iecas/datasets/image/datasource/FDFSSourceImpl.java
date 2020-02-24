package cn.iecas.datasets.image.datasource;

import cn.iecas.datasets.image.dao.TileInfosMapper;
import cn.iecas.datasets.image.pojo.domain.TileInfosDO;
import cn.iecas.datasets.image.pojo.dto.TileSetDTO;
import cn.iecas.datasets.image.pojo.entity.Tile;
import cn.iecas.datasets.image.utils.FastDFSUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import org.csource.common.MyException;
import org.csource.fastdfs.StorageClient1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class FDFSSourceImpl implements BaseDataSource {
    @Value("${value.fastdfs.server}")
    private String fastdfsServer;   //FastDFS服务路径
    @Autowired
    public TileInfosMapper tileInfosMapper;

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
    public Tile getImageByName(String visualPath) {
        Tile tile = new Tile();
        StorageClient1 storageClient = FastDFSUtil.getSrorageClient();
        byte[] result = null;
        try {
            result = storageClient.download_file1(visualPath);
//            printTile(result,"d:\\test.jpg",500);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        BASE64Encoder encoder = new BASE64Encoder();
        String imageDataString = encoder.encode(result).replaceAll("\r|\n", "");
        String base64Tile = "data:image/jpeg;base64," + imageDataString;
        tile.setBase64Tile(base64Tile);
        return tile;
    }


    public static void printTile(byte[] tileBuffer,String path,int tileSize){
        FileOutputStream fs;
        try {
            fs = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return ;
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(tileBuffer);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedImage image = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_RGB);
        try {
            image.getGraphics().drawImage(ImageIO.read(inputStream), 0, 0, tileSize, tileSize, null);
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(bos);
            encoder.encode(image);
            fs.write(bos.toByteArray());
            fs.flush();
            fs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
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
    public void deletes(int tileId) throws Exception {
        String storagePath;
        String visualPath;
        String labelPath;

        TileInfosDO tileInfosDO = tileInfosMapper.getTileByName(tileId);
        if (tileInfosDO != null){
            storagePath = tileInfosDO.getStoragePath();
            visualPath = tileInfosDO.getVisualPath();
            labelPath = tileInfosDO.getLabelPath();

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
     * @param tileInfosDOS
     * @return
     */
    @Override
    public List<Tile> getImages(List<TileInfosDO> tileInfosDOS) throws Exception {
//        TileSetDTO tileSetDTO = new TileSetDTO();
        List<Tile> tileList = new ArrayList<>();

        StorageClient1 storageClient = FastDFSUtil.getSrorageClient();
        BASE64Encoder encoder = new BASE64Encoder();
        byte[] result;
        String storagePath;

        for (TileInfosDO tileInfosDO : tileInfosDOS){
            storagePath = tileInfosDO.getStoragePath();   //存储路径
            if (storagePath != null){
                result = storageClient.download_file1(storagePath);
                if (result != null){
                    String encodedimageData = encoder.encode(result);
                    String imageBase64String = ("data:image/jpeg;base64," + encodedimageData).replaceAll("\r|\n", "");

                    Tile tile = new Tile();
                    tile.setBase64Tile(imageBase64String);
                    tile.setId(tileInfosDO.getId());
                    tile.setName(tileInfosDO.getName());
                    tile.setCreateTime(tileInfosDO.getCreateTime());
                    tileList.add(tile);
                }else {
                    continue;
                }
            } else {
                return null;
            }
        }

        return tileList;
    }

}
