package cn.iecas.datasets.image.controller;

import cn.iecas.datasets.image.annotation.Log;
import cn.iecas.datasets.image.pojo.domain.TileInfosDO;
import cn.iecas.datasets.image.pojo.dto.CommonResponseDTO;
import cn.iecas.datasets.image.pojo.dto.TileInfoAllStatisticResponseDTO;
import cn.iecas.datasets.image.pojo.dto.TileInfoStatParamsDTO;
import cn.iecas.datasets.image.pojo.dto.TileInfoStatisticResponseDTO;
import cn.iecas.datasets.image.pojo.entity.TileInfoStatistic;
import cn.iecas.datasets.image.service.TileInfosService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Slf4j
@RequestMapping("/tileinfo")
public class TileInfoController {

    @Autowired
    TileInfosService tileInfosService;
    /**
     * http://localhost:28000/geoapi/V1/datasets/tileinfo/add
     * 批量添加切片数据
     * @param tileInfoDOS
     * @return
     */
    @Log("批量增加切片数据，并且更新对应数据集")
    @PostMapping(value = "/add")
    public CommonResponseDTO addTileinfos(@RequestBody List<TileInfosDO> tileInfoDOS){
        tileInfosService.insertTileInfos(tileInfoDOS);
        return new CommonResponseDTO().success().message("批量增加切片数据成功");
    }


    /**
     * @api {get} /tileinfos/statistic 根据年月日统计累计切片数量
     * @apiDescription 根据年月日统计累计切片数量
     * @apiName statistic
     * @apiGroup 切片
     * @apiVersion 1.0.0
     * @apiExample {curl} Example usage:
     *     http://localhost/geoapi/datasets/tileinfos/statistic?timeStep=month
     *
     *
     * @apiParam {String="year","month","day"} [timeStep=month] 统计时间方式
     * @apiParam {long} [endTime="服务器时间戳"] 统计结束时间
     * @apiParam {long} [beginTime="2019-1-1"] 统计开始时间
     * @apiParam {List<String>}  [imageSetIds="1,2,3"] 统计数据集
     * @apiSuccess {int} count 返回的统计分段数
     * @apiSuccess {Object[]} content 统计分段信息内容
     *
     *
     *

     */
    @Log("根据数据集id返回返回属于该id的切片的年月日数据增长信息，当不指定id是则默认返回全部")
    @GetMapping(value = "/statistic")
    public CommonResponseDTO statistic(TileInfoStatParamsDTO statParams){
        if(statParams.getImagesetid()!=null){
            TileInfoStatisticResponseDTO tileInfoStatisticResponseDTO=tileInfosService.getStatisticByIds(statParams);
            return new CommonResponseDTO().success().data(tileInfoStatisticResponseDTO).message("统计数据集切片信息成功");
        }else{
            TileInfoAllStatisticResponseDTO tileInfoAllStatisticResponseDTO=tileInfosService.getStatistic(statParams);
            return new CommonResponseDTO().success().data(tileInfoAllStatisticResponseDTO).message("统计全部切片信息成功");
        }


    }
}
