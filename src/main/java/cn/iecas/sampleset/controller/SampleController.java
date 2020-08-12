package cn.iecas.sampleset.controller;

import cn.iecas.sampleset.common.annotation.Log;
import cn.iecas.sampleset.pojo.dto.*;
import cn.iecas.sampleset.pojo.dto.common.CommonResult;
import cn.iecas.sampleset.pojo.dto.common.PageResult;
import cn.iecas.sampleset.pojo.dto.request.SampleSetTransferParams;
import cn.iecas.sampleset.pojo.dto.response.SampleTransferStatus;
import cn.iecas.sampleset.pojo.entity.Sample;
import cn.iecas.sampleset.pojo.enums.SampleType;
import cn.iecas.sampleset.service.TransferService;
import cn.iecas.sampleset.service.SampleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/sample")
public class SampleController {
    @Autowired
    TransferService transferService;

    @Autowired
    SampleService sampleService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Log("根据样本主键批量删除")
    @DeleteMapping("/{sampleIdList}")
    public CommonResult<String> deleteTiles(@NotEmpty @PathVariable List<Integer> sampleIdList) throws Exception {
        this.sampleService.deleteSamples(sampleIdList);
        return new CommonResult<String>().success().message("删除样本数据成功!");
    }

    @GetMapping
    @Log("分页查询样本数据")
    public CommonResult<PageResult<Sample>> listSampleBySetId(SampleRequestParams sampleRequestParams) throws Exception {
        PageResult<Sample> samplePageResult = this.sampleService.listSamplesBySetId(sampleRequestParams);
        return new CommonResult<PageResult<Sample>>().success().data(samplePageResult).message("查询样本数据成功");

    }

    @Log("查询指定影像")
    @GetMapping(value = "/{sampleType}/{sampleId}")
    public CommonResult<Sample> getImageByName(@PathVariable int sampleId, @PathVariable SampleType sampleType ) throws Exception {
        Sample sample = this.sampleService.getSampleByTypeAndId(sampleId, sampleType);
        return new CommonResult<Sample>().success().data(sample).message("查询影像数据成功");
    }

    @PostMapping(value = "/upload")
    @Log("分块断点上传切片压缩包")
    public CommonResult<SampleSetTransferParams> uploadTiles(HttpServletRequest request, SampleSetTransferParams sampleSetTransferParams) throws Exception {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart)
            return new CommonResult<SampleSetTransferParams>().data(sampleSetTransferParams).fail().message("请选择上传文件");

        sampleSetTransferParams = this.sampleService.uploadTiles(sampleSetTransferParams);
        return new CommonResult<SampleSetTransferParams>().data(sampleSetTransferParams).success().message("上传分块结束");
    }


    @Log("秒传判断，断点判断")
    @GetMapping(value = "/checkFileMd5")
    public CommonResult<SampleTransferStatus> checkFileMd5(int sampleSetId, String md5) throws Exception {
        SampleTransferStatus sampleTransferStatus = transferService.checkFileMd5(sampleSetId,md5);
        return new CommonResult<SampleTransferStatus>().success().data(sampleTransferStatus).message("检查md5结束");
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
    public CommonResult statistic(TileInfoStatParamsDTO statParams){
        if(statParams.getImageSetIdList()!=null){
            TileInfoStatisticResponseDTO tileInfoStatisticResponseDTO= sampleService.getStatisticByIds(statParams);
            return new CommonResult().success().data(tileInfoStatisticResponseDTO).message("统计数据集切片信息成功");
        }else{
            TileInfoAllStatisticResponseDTO tileInfoAllStatisticResponseDTO= sampleService.getStatistic(statParams);
            return new CommonResult().success().data(tileInfoAllStatisticResponseDTO).message("统计全部切片信息成功");
        }
    }

}
