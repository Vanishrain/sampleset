package cn.iecas.datasets.image.service;

import cn.iecas.datasets.image.common.domain.QueryRequest;
import cn.iecas.datasets.image.pojo.domain.TileInfosDO;
import cn.iecas.datasets.image.pojo.entity.uploadFile.MultipartFileParam;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.csource.common.MyException;

import java.io.IOException;
import java.util.List;

/**
 * 存储操作的service
 */
public interface StorageService {
    /**
     * 上传文件方法
     * 处理文件分块，基于MappedByteBuffer来实现文件的保存
     */
    Object uploadFileByMappedByteBuffer(MultipartFileParam param) throws IOException;

    void download(TileInfosDO tileInfosDO) throws IOException, MyException;
    String getByName(TileInfosDO tileInfosDO) throws IOException;
    List<String> getAll(QueryRequest request) throws IOException;
}
