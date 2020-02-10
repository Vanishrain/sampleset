package cn.iecas.datasets.image.controller;

import cn.iecas.datasets.image.annotation.Log;
import cn.iecas.datasets.image.common.controller.BaseController;
import cn.iecas.datasets.image.datasource.BaseDataSource;
import cn.iecas.datasets.image.pojo.domain.ImageDataSetInfoDO;
import cn.iecas.datasets.image.pojo.dto.CommonResponseDTO;
import cn.iecas.datasets.image.pojo.dto.ImageDataSetInfoDTO;
import cn.iecas.datasets.image.pojo.dto.ImageDataSetInfoRequestDTO;
import cn.iecas.datasets.image.pojo.dto.ImageDataSetStatisticDTO;
import cn.iecas.datasets.image.service.ImageDataSetsService;
import cn.iecas.datasets.image.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * @author vanishrain
 */
@Slf4j
@RestController
@RequestMapping("/image")
public class ImageDatasetsController extends BaseController {
    @Autowired
    StorageService storageService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ImageDataSetsService imageDatasetsService;

    @Autowired
    BaseDataSource baseDataSource;


    @Log("查询样本集信息详情")
    @GetMapping(value = "/detail")
    @CrossOrigin
    public CommonResponseDTO listImageDatasetInfos(ImageDataSetInfoRequestDTO imageDatasetInfoRequestDTO){
        ImageDataSetInfoDTO imageDataSetInfoDTO = imageDatasetsService.listImageDatasetInfoDetail(imageDatasetInfoRequestDTO);
        return new CommonResponseDTO().success().data(imageDataSetInfoDTO).message("查询样本集信息成功");
    }

    @Log("删除指定id的影像数据集")
    @DeleteMapping(value = "/{idList}")
    @CrossOrigin
    public CommonResponseDTO deleteImageDataSetById(@PathVariable String idList) throws Exception {
        List<String> dataSetIdList= Arrays.asList(idList.split(","));
        imageDatasetsService.deleteImageDataSetByIds(dataSetIdList);
        return new CommonResponseDTO().success().message("成功删除影像数据信息还有对应的切片信息");
    }

    /**
     * http://localhost:28000/geoapi/V1/datasets/image/add
     * @param imageDataSetInfoDO
     * @return
     */
    @Log("增加影像数据集信息")
    @PostMapping(value = "/add")
    @CrossOrigin
    public CommonResponseDTO addImageDataSet(@RequestBody ImageDataSetInfoDO imageDataSetInfoDO){
        imageDatasetsService.insertImageDataSet(imageDataSetInfoDO);
        return new CommonResponseDTO().success().message("成功插入影像数据信息");
    }

    @Log("更新影像数据集信息")
    @PutMapping()
    @CrossOrigin
    public CommonResponseDTO updateImageDataSetInfo(@RequestBody ImageDataSetInfoDO imageDataSetInfoDO){
        this.imageDatasetsService.updateImageDataSetInfoById(imageDataSetInfoDO);
        return new CommonResponseDTO().success().message("成功更新影像数据信息");
    }

    @Log("查看影像数据集统计信息")
    @GetMapping(value = "/statistic")
    @CrossOrigin
    public CommonResponseDTO getStatistic(){
        ImageDataSetStatisticDTO imageDataSetStatisticDTO = imageDatasetsService.getStatistic();
        return new CommonResponseDTO().success().data(imageDataSetStatisticDTO).message("成功获取影像数据集统计信息");
    }
}
