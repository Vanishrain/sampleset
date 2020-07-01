package cn.iecas.sampleset.service;

import cn.iecas.sampleset.pojo.domain.Image;

import java.util.List;

public interface ImageService {
    List<Image> listImageInfoByIdList(List<Integer> imageIdList);
}
