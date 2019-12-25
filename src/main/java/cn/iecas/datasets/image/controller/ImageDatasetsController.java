package cn.iecas.datasets.image.controller;

import cn.iecas.datasets.image.annotation.Log;
import cn.iecas.datasets.image.common.controller.BaseController;
import cn.iecas.datasets.image.datasource.BaseDataSource;
import cn.iecas.datasets.image.pojo.domain.ImageDataSetInfoDO;
import cn.iecas.datasets.image.pojo.dto.CommonResponseDTO;
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


    @Log("查询影像数据集信息详情")
    @GetMapping(value = "/detail")
    public CommonResponseDTO listImageDatasetInfos(ImageDataSetInfoRequestDTO imageDatasetInfoRequestDTO){
        List<ImageDataSetInfoDO> imageDataSetInfoDOList = imageDatasetsService.listImageDatasetInfoDetail(imageDatasetInfoRequestDTO);
        return new CommonResponseDTO().success().data(imageDataSetInfoDOList);
    }

//    @GetMapping(value = "/visualimg/{datasetId}/{imageName}")
//    public CommonResponseDTO<Tile> getLabeledImg(@PathVariable int datasetId, @PathVariable String imageName){
//        imageName = imageName.contains("mt") ? imageName.replaceAll("mt", "%") : imageName;
//        Tile image = imageDatasetsService.getImageByName(datasetId, imageName, "visual");
//        return new CommonResponseDTO<Tile>().success().data(image).message("查询标注影像数据成功");
//    }

    //deleteImageDataSetByIds 未实现删除fastdfs中的数据
    @Log("删除指定id的影像数据集")
    @DeleteMapping(value = "/{idList}")
    public CommonResponseDTO deleteImageDataSetById(@PathVariable String idList) throws Exception {
        List<String> dataSetIdList= Arrays.asList(idList.split(","));
        imageDatasetsService.deleteImageDataSetByIds(dataSetIdList);
        return new CommonResponseDTO().success().message("成功删除影像数据信息还有对应的切片信息,或者要删除的数据集本身就不存在");
    }

    /**
     * http://localhost:28000/geoapi/V1/datasets/image/add
     * @param imageDataSetInfoDO
     * @return
     */
    @Log("增加指定id的影像数据集")
    @PostMapping(value = "/add")
    public CommonResponseDTO addImageDataSet(ImageDataSetInfoDO imageDataSetInfoDO){
        imageDatasetsService.insertImageDataSet(imageDataSetInfoDO);
        return new CommonResponseDTO().success().message("成功插入影像数据信息");
    }

    @Log("更新影像数据集信息")
    @PutMapping()
    public CommonResponseDTO updateImageDataSetInfo(ImageDataSetInfoDO imageDataSetInfoDO){
        this.imageDatasetsService.updateImageDataSetInfoById(imageDataSetInfoDO);
        return new CommonResponseDTO().success().message("成功更新影像数据信息");
    }

    @Log("查看影像数据集统计信息")
    @GetMapping(value = "/statistic")
    public CommonResponseDTO getStatistic(){
        ImageDataSetStatisticDTO imageDataSetStatisticDTO = imageDatasetsService.getStatistic();
        return new CommonResponseDTO().success().data(imageDataSetStatisticDTO).message("成功获取影像数据集统计信息");
    }
}
