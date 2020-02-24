package cn.iecas.datasets.image.common.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.HashMap;
import java.util.Map;

public class BaseController {
    protected Map<String, Object> getDataTable(IPage<?> pageInfo){
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("rows", pageInfo.getRecords());
        resultData.put("total", pageInfo.getTotal());

        return resultData;
    }
}
