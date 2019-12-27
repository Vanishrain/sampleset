package cn.iecas.datasets.image.controller;

import cn.iecas.datasets.image.annotation.Log;
import cn.iecas.datasets.image.pojo.dto.*;
import cn.iecas.datasets.image.pojo.entity.Tile;
import cn.iecas.datasets.image.pojo.entity.uploadFile.MultipartFileParam;
import cn.iecas.datasets.image.pojo.entity.uploadFile.ResultStatus;
import cn.iecas.datasets.image.pojo.entity.uploadFile.ResultVo;
import cn.iecas.datasets.image.service.StorageService;
import cn.iecas.datasets.image.service.TileInfosService;
import cn.iecas.datasets.image.utils.Constants;
import cn.iecas.datasets.image.utils.FastDFSUtil;
import cn.iecas.datasets.image.utils.FileMD5Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.websocket.server.PathParam;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


@RestController
@Slf4j
@RequestMapping("/tiles")
public class TileInfoController {

    @Autowired
    private  HttpServletRequest request;

    @Autowired
    TileInfosService tileInfosService;
    @Autowired
    StorageService storageService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Log("根据切片主键批量删除")
    @DeleteMapping("/{tileIds}")
    public CommonResponseDTO deleteImages(@NotEmpty @PathVariable Integer[] tileIds){
        tileInfosService.deleteImages(tileIds);
        return new CommonResponseDTO().success().message("删除成功!");
    }

    @Log("分页查询切片数据")
    @GetMapping
    public CommonResponseDTO listImagesByDatasetId(TileRequestDTO tileRequestDTO){
        TileSetDTO tileSetDTO = tileInfosService.listTilesByDataSetId(tileRequestDTO);
        return new CommonResponseDTO().success().data(tileSetDTO).message("查询影像数据成功");
    }

    @Log("查询指定名称的影像")
    @GetMapping(value = "/{tileId}/{type}")
    public CommonResponseDTO<Tile> getImageByName(@PathVariable String tileId, @PathVariable String type ){
        Tile tile = tileInfosService.getTileByName(tileId, type);
        return new CommonResponseDTO<Tile>().success().data(tile).message("查询影像数据成功");
    }

    @Log("批量增加切片数据，并且更新对应数据集")
    @PostMapping(value = "/upload")
    @CrossOrigin
    public CommonResponseDTO uploadTiles(MultipartFileParam param) throws Exception {
        storageService.uploadTiles(param, request);
        return new CommonResponseDTO().success().message("成功上传");
    }

    @Log("下载切片压缩包")
    @GetMapping("/downloadTile/{imagesetid}")
    public CommonResponseDTO downloadTile(@NotEmpty @PathVariable int imagesetid) throws IOException {
        storageService.download(imagesetid);
        return new CommonResponseDTO().success().message("下载完成！");
    }

    @Log("秒传判断，断点判断")
    @GetMapping(value = "checkFileMd5")
    @CrossOrigin
    public CommonResponseDTO checkFileMd5(String md5) {
        ResultVo resultVo = FileMD5Util.checkFileMd5(md5);
        return new CommonResponseDTO().success().data(resultVo).message(resultVo.getMsg());
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
