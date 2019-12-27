package cn.iecas.datasets.image.service;

import cn.iecas.datasets.image.pojo.entity.uploadFile.MultipartFileParam;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 存储操作的service
 */
public interface StorageService {
    void uploadTiles(MultipartFileParam param, HttpServletRequest request) throws Exception;

    void download(int imagesetid) throws IOException;
}
