package cn.iecas.sampleset.datasource;

import cn.iecas.sampleset.pojo.domain.SampleInfo;
import cn.iecas.sampleset.pojo.entity.Sample;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface BaseDataSource {

    /**
     * 关闭数据源连接
     */
    void close();

    /**
     * 初始化数据源连接
     */
    void initialize();

    /**
     * 根据数据集id删除全部切片数据
     * @param imageSetId
     */
    void deleteImageSetById(int imageSetId);

    /**
     * 根据切片名称获取数据集中的切片
     * @param imageName
     * @return
     */
    Sample getImageByName(String imageName);

    String getImageByPath( String path);

    void deletes(SampleInfo sampleInfo) throws Exception;
    byte[] download(String fileId) throws Exception;

    /**
     * 根据切片名称删除数据集中的切片
     * @param imageSetId
     * @param imageName
     */
    void deleteImageByName(int imageSetId, String imageName);


    /**
     * 分页获取数据集切片
     * @param sampleInfos
     * @return
     */
    List<Sample> getImages(List<SampleInfo> sampleInfos) throws Exception;
}
