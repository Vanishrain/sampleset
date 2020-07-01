//package cn.iecas.datasets.image.datasource;
//
//import cn.iecas.datasets.image.pojo.dto.TileSetDTO;
//import cn.iecas.datasets.image.pojo.entity.Tile;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.io.FilenameUtils;
//import org.springframework.stereotype.Component;
//import sun.misc.BASE64Encoder;
//
//import java.io.*;
//import java.util.ArrayList;
//import java.util.List;
//
//@Slf4j
//@Component
//public class FSDataSourceImpl implements BaseDataSource {
//    @Override
//    public void close() {
//
//    }
//
//    @Override
//    public void initialize() {
//
//    }
//
//    @Override
//    public TileSetDTO getImages(String path) {
//        return getImages(path, 1, 10);
//    }
//
//    @Override
//    public long getSizeByDirectory(String path) {
//        return 0;
//    }
//
//    @Override
//    public Tile getImageByName(String path, String imageName) {
//        Tile image = new Tile();
//        String imageFilePath = FilenameUtils.normalize(path + File.separator + imageName);
//        String base64Tile = getImageBase64(imageFilePath);
//        image.setName(imageName);
//        image.setBase64Tile(base64Tile);
//        return image;
//    }
//
//
//    @Override
//    public TileSetDTO getImages(String path, int pageNo, int pageSize) {
//        TileSetDTO imageSetDTO = new TileSetDTO();
//        pageNo = pageNo != 0 ? pageNo : 1;
//        pageSize = pageSize !=0 ? pageSize : 10;
//        List<Tile> tileList = new ArrayList<>();
//
//        List<String> imagePathList = getImagePaths(path, pageNo, pageSize);
//        for (String imagePath : imagePathList){
//            Tile image = new Tile();
//            File file = new File(imagePath);
//            String fileName = file.getName().substring(0,file.getName().indexOf("."));
//            if(fileName.contains("%"))
//                fileName = fileName.replaceAll("%","mt");
//
//            String imageBase64 = getImageBase64(imagePath);
//
//            image.setName(fileName);
//            image.setBase64Tile(imageBase64);
//            tileList.add(image);
//        }
//
//        imageSetDTO.setPageNo(pageNo);
//        imageSetDTO.setTileList(tileList);
//
//        return imageSetDTO;
//    }
//
//
//
//    /**
//     * 获取影像文件的路径
//     * @param path
//     * @param pageNo
//     * @param pageSize
//     * @return
//     */
//    private List<String> getImagePaths(String path, int pageNo, int pageSize) {
//        File file = new File(path);
//        int toIndex = pageNo + pageSize;
//        List<String> imagePathList = new ArrayList<>();
//
//        File[] subFiles = file.listFiles();
//        for (File subFile : subFiles) {
//            if (subFile.getParent().endsWith(".jpg"))
//                imagePathList.add(subFile.getParent());
//        }
//
//        int totalCount = imagePathList.size();
//        toIndex = Math.min(toIndex, totalCount);
//        int fromIndex = toIndex - pageSize;
//        imagePathList = imagePathList.subList(fromIndex , toIndex);
//        return imagePathList;
//    }
//
//    /**
//     * 获取影像的base64编码
//     * @param imagePath
//     * @return
//     */
//    private String getImageBase64(String imagePath){
//        File file = new File(imagePath);
//        byte[] buffer = new byte[1024];
//
//        try {
//            InputStream inputStream = new FileInputStream(file);
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            int numBytesRead = 0;
//            while ((numBytesRead = inputStream.read(buffer)) != -1) {
//                out.write(buffer, 0, numBytesRead);
//            }
//
//            BASE64Encoder encoder = new BASE64Encoder();
//            return encoder.encode(out.toByteArray());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }
//
//    @Override
//    public long getCountByDirectory(String path, String... postfix) {
//        return 0;
//    }
//}
