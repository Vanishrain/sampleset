package cn.iecas.sampleset.service;


import cn.iecas.sampleset.pojo.dto.UserInfo;
import cn.iecas.sampleset.pojo.dto.common.CommonResult;

public interface UserInfoService {
    CommonResult<UserInfo> getUserInfo(String token);
}
