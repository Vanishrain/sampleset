package cn.iecas.sampleset.service.impl;

import cn.iecas.sampleset.pojo.dto.UserInfo;
import cn.iecas.sampleset.pojo.dto.common.CommonResult;
import cn.iecas.sampleset.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Value("${value.api.user-info}")
    private String userInfoApi;

    @Autowired
    private RestTemplate restTemplate;


    /**
     * 通过token获取用户信息
     * @param token
     * @return
     */
    @Override
    public CommonResult<UserInfo> getUserInfo(String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("token",token);
        HttpEntity<String> httpEntity = new HttpEntity<>(null,httpHeaders);
        CommonResult commonResult = restTemplate.exchange(userInfoApi, HttpMethod.GET,httpEntity,CommonResult.class).getBody();
        return commonResult;
    }
}
