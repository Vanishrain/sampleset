package cn.iecas.sampleset.service.impl;

import cn.iecas.sampleset.pojo.domain.Image;
import cn.iecas.sampleset.service.ImageService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * 访问影像服务接口
 */
@Slf4j
@Service
@Transactional
public class ImageServiceImpl implements ImageService {

    @Value(value = "${value.api.image-info}")
    private String imageInfoApi;

    @Autowired
    private RestTemplate restTemplate;


    @Override
    public List<Image> listImageInfoByIdList(List<Integer> imageIdList) throws ResourceAccessException {
        if (imageIdList.size()==0)
            return null;

        JSONObject result = null;
        String imageIds = imageIdList.toString().replace("[","").replace("]","").replace(" ","");

        try{
            result = this.restTemplate.getForObject(imageInfoApi+imageIds, JSONObject.class);
        }catch (ResourceAccessException e){
            log.error("访问影像服务接口api：{} 出错",imageInfoApi+imageIds);
            throw new ResourceAccessException("关联服务访问出错");
        }
        return JSONArray.parseArray(result.getJSONArray("data").toJSONString(),Image.class);
    }
}