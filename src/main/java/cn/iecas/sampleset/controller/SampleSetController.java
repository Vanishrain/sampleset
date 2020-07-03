package cn.iecas.sampleset.controller;

import cn.iecas.sampleset.common.annotation.Log;
import cn.iecas.sampleset.pojo.domain.SampleSetInfo;
import cn.iecas.sampleset.pojo.dto.SampleSetCreationInfo;
import cn.iecas.sampleset.pojo.dto.SampleSetInfoRequestParam;
import cn.iecas.sampleset.pojo.dto.common.CommonResult;
import cn.iecas.sampleset.pojo.dto.SampleSetStatistic;
import cn.iecas.sampleset.pojo.dto.common.PageResult;
import cn.iecas.sampleset.service.SampleSetService;
import cn.iecas.sampleset.service.TransferService;
import cn.iecas.sampleset.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
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

    @GetMapping
    @Log("查询样本集信息详情")
    public CommonResult<PageResult<SampleSetInfo>> listImageDatasetInfos(SampleSetInfoRequestParam sampleSetInfoRequestParam){
        PageResult<SampleSetInfo> sampleSetPageResult = this.sampleSetService.listSampleSetInfos(sampleSetInfoRequestParam);
        return new CommonResult<PageResult<SampleSetInfo>>().success().data(sampleSetPageResult).message("查询样本集信息成功");
    }

    @Log("删除指定id的影像数据集")
    @DeleteMapping(value = "/{sampleSetIdList}")
    public CommonResult<String> deleteSampleSetById(@PathVariable List<Integer> sampleSetIdList) throws Exception {
        this.sampleSetService.deleteSampleSetByIds(sampleSetIdList);
        return new CommonResult<String>().success().message("成功删除影像数据信息还有对应的切片信息");
    }

    @Log("创建新的样本集信息")
    @PostMapping
    public CommonResult<String> createSampleSet(@RequestBody SampleSetInfo sampleSetInfo){
        sampleSetInfo.setCreateTime(DateUtil.nowDate());
        this.sampleSetService.save(sampleSetInfo);
        return new CommonResult<String>().success().message("成功插入样本集信息");
    }

    @PutMapping
    @Log("更新样本集信息")
    public CommonResult<String> updateSampleSetInfo(@RequestBody SampleSetInfo sampleSetInfo){
        this.sampleSetService.updateById(sampleSetInfo);
        return new CommonResult<String>().success().message("成功更新样本集数据信息");
    }

    @Async
    @PostMapping(value = "/creation")
    @Log("从数据集创建样本集的接口")
    public CommonResult<String> createSampleSetFromDataset(@RequestBody SampleSetCreationInfo sampleSetCreationInfo) throws IOException {
        this.sampleSetService.createSampleSet(sampleSetCreationInfo);
        return new CommonResult<String>().success().message("生成样本集成功");
    }

    @Log("下载切片压缩包")
    @GetMapping(value = "/download/{sampleSetId}")
    public void downloadSampleSet(@NotEmpty @PathVariable int sampleSetId) throws Exception {
        this.transferService.download(sampleSetId);
    }

    //todo 重构样本统计信息
    @Log("查看影像数据集统计信息")
    @GetMapping(value = "/statistic")
    public CommonResult<SampleSetStatistic> getStatistic(){
        SampleSetStatistic sampleSetStatistic = sampleSetService.getStatistic();
        return new CommonResult<SampleSetStatistic>().success().data(sampleSetStatistic).message("成功获取影像数据集统计信息");
    }
}
