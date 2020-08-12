package cn.iecas.sampleset.service;

import cn.iecas.sampleset.pojo.domain.Image;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

public interface ImageService {
    List<Image> listImageInfoByIdList(List<Integer> imageIdList,String token) throws ResourceAccessException;
}
