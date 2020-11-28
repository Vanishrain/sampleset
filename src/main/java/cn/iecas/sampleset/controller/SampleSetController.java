package cn.iecas.sampleset.controller;

import cn.iecas.sampleset.common.annotation.ControllerLog;
import cn.iecas.sampleset.pojo.domain.SampleSetInfo;
import cn.iecas.sampleset.pojo.dto.SampleSetCreationInfo;
import cn.iecas.sampleset.pojo.dto.SampleSetInfoRequestParam;
import cn.iecas.sampleset.pojo.dto.common.CommonResult;
import cn.iecas.sampleset.pojo.dto.SampleSetStatistic;
import cn.iecas.sampleset.pojo.dto.common.PageResult;
import cn.iecas.sampleset.service.SampleSetService;
import cn.iecas.sampleset.service.TransferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author vanishrain
 */
@Slf4j
@RestController
@RequestMapping("/sampleset")
public class SampleSetController {
    @Autowired
    TransferService transferService;

    @Autowired
    SampleSetService sampleSetService;

    @Autowired
    HttpServletRequest servletRequest;

    @GetMapping("/list")
    @ControllerLog("列出用户所有的样本集信息")
    public CommonResult<List<SampleSetInfo>> listSampleSetInfo(int userId){
        List<SampleSetInfo> sampleSetInfoList = this.sampleSetService.listSampleSetInfo(userId);
        return new CommonResult<List<SampleSetInfo>>().success().data(sampleSetInfoList).message("查询样本集信息成功");
    }

    @GetMapping
    @ControllerLog("查询样本集信息详情")
    public CommonResult<PageResult<SampleSetInfo>> listSampleSetInfoByPage(SampleSetInfoRequestParam sampleSetInfoRequestParam){
        PageResult<SampleSetInfo> sampleSetPageResult = this.sampleSetService.listSampleSetInfos(sampleSetInfoRequestParam);
        return new CommonResult<PageResult<SampleSetInfo>>().success().data(sampleSetPageResult).message("查询样本集信息成功");
    }

    @ControllerLog("删除指定id的影像数据集")
    @DeleteMapping(value = "/{sampleSetIdList}")
    public CommonResult<String> deleteSampleSetById(@PathVariable List<Integer> sampleSetIdList) throws Exception {
        this.sampleSetService.deleteSampleSetByIds(sampleSetIdList);
        return new CommonResult<String>().success().message("成功删除影像数据信息还有对应的切片信息");
    }

    @ControllerLog("创建新的样本集信息")
    @PostMapping
    public CommonResult<String> createSampleSet(@RequestBody SampleSetInfo sampleSetInfo){
        this.sampleSetService.createSampleSet(sampleSetInfo);
        return new CommonResult<String>().success().message("成功插入样本集信息");
    }

    @PutMapping
    @ControllerLog("更新样本集信息")
    public CommonResult<String> updateSampleSetInfo(@RequestBody SampleSetInfo sampleSetInfo){
        this.sampleSetService.updateById(sampleSetInfo);
        return new CommonResult<String>().success().message("成功更新样本集数据信息");
    }

    @PostMapping(value = "/creation")
    @ControllerLog("从数据集创建样本集的接口")
    public CommonResult<String> createSampleSetFromDataset(@RequestBody SampleSetCreationInfo sampleSetCreationInfo) throws IOException {
        String token = servletRequest.getHeader("token");
        this.sampleSetService.createSampleSet(sampleSetCreationInfo,token);
        return new CommonResult<String>().success().message("生成样本集成功");
    }

    @ControllerLog("下载切片压缩包")
    @GetMapping(value = "/download")
    public void downloadSampleSet(int sampleSetId, HttpServletResponse response) throws Exception {
        this.sampleSetService.downloadSampleSet(sampleSetId,response);
    }

    //todo 重构样本统计信息
    @ControllerLog("查看影像数据集统计信息")
    @GetMapping(value = "/statistic")
    public CommonResult<SampleSetStatistic> getStatistic(){
        SampleSetStatistic sampleSetStatistic = sampleSetService.getStatistic();
        return new CommonResult<SampleSetStatistic>().success().data(sampleSetStatistic).message("成功获取影像数据集统计信息");
    }
}
