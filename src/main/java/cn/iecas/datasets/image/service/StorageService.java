package cn.iecas.datasets.image.service;

import cn.iecas.datasets.image.pojo.domain.TileInfosDO;
import cn.iecas.datasets.image.pojo.entity.uploadFile.MultipartFileParam;
import org.csource.common.MyException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 存储操作的service
 */
public interface StorageService {
    Object uploadTiles(MultipartFileParam param, HttpServletRequest request);

    void download(TileInfosDO tileInfosDO) throws IOException, MyException;
}
