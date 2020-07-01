package cn.iecas.sampleset.service.impl;

import cn.iecas.sampleset.pojo.domain.Image;
import cn.iecas.sampleset.service.ImageService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 *
 */
@Service
@Transactional
public class ImageServiceImpl implements ImageService {

    @Value(value = "${value.api.image-info}")
    String imageInfoApi;


    @Override
    public List<Image> listImageInfoByIdList(List<Integer> imageIdList) {
        if (imageIdList.size() == 0)
            return null;
        String imageIds = imageIdList.toString().replace("[", "").replace("]", "").replace(" ", "");
        RestTemplate restTemplate = new RestTemplate();
        JSONObject result = restTemplate.getForObject(imageInfoApi + imageIds
                , JSONObject.class);
        return JSONArray.parseArray(result.getJSONArray("data").toJSONString(), Image.class);
    }
}