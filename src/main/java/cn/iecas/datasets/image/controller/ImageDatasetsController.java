package cn.iecas.datasets.image.controller;

import cn.iecas.datasets.image.annotation.Log;
import cn.iecas.datasets.image.common.controller.BaseController;
import cn.iecas.datasets.image.common.domain.QueryRequest;
import cn.iecas.datasets.image.pojo.domain.ImageDataSetInfoDO;
import cn.iecas.datasets.image.pojo.domain.TileInfosDO;
import cn.iecas.datasets.image.pojo.dto.*;
import cn.iecas.datasets.image.pojo.entity.Image;
import cn.iecas.datasets.image.pojo.entity.uploadFile.MultipartFileParam;
import cn.iecas.datasets.image.pojo.entity.uploadFile.ResultStatus;
import cn.iecas.datasets.image.pojo.entity.uploadFile.ResultVo;
import cn.iecas.datasets.image.service.ImageDataSetsService;
import cn.iecas.datasets.image.service.StorageService;
import cn.iecas.datasets.image.utils.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.csource.common.MyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author vanishrain
 */
@Slf4j
@RestController
@RequestMapping("/image")
public class ImageDatasetsController extends BaseController {
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    StorageService storageService;
    @Autowired
    ImageDataSetsService imageDatasetsService;

    @Log("查询数据集影像")
    @GetMapping(value = "/images")
    public CommonResponseDTO listImagesByDatasetId(ImageRequestDTO imageRequestDTO){
        ImageSetDTO imageSetDTO = imageDatasetsService.listImagesByDataSetId(imageRequestDTO);
        return new CommonResponseDTO().success().data(imageSetDTO).message("查询影像数据成功");
    }

    @Log("查询指定数据集指定名称的影像")
    @GetMapping(value = "/images/{datasetId}/{imageName}")
    public CommonResponseDTO<Image> getImageByName(@PathVariable int datasetId, @PathVariable String imageName){
        Image image = imageDatasetsService.getImageByName(datasetId, imageName, "imgs");
        return new CommonResponseDTO<Image>().success().data(image).message("查询影像数据成功");
    }

    @Log("查询影像数据集信息详情")
    @GetMapping(value = "/detail")
    public CommonResponseDTO listImageDatasetInfos(ImageDataSetInfoRequestDTO imageDatasetInfoRequestDTO){
        List<ImageDataSetInfoDO> imageDataSetInfoDOList = imageDatasetsService.listImageDatasetInfoDetail(imageDatasetInfoRequestDTO);
        return new CommonResponseDTO().success().data(imageDataSetInfoDOList);
    }

    @GetMapping(value = "/visualimg/{datasetId}/{imageName}")
    public CommonResponseDTO<Image> getLabeledImg(@PathVariable int datasetId, @PathVariable String imageName){
        imageName = imageName.contains("mt") ? imageName.replaceAll("mt", "%") : imageName;
        Image image = imageDatasetsService.getImageByName(datasetId, imageName, "visual");
        return new CommonResponseDTO<Image>().success().data(image).message("查询标注影像数据成功");
    }

    /**
     *http://localhost:28000/geoapi/V1/datasets/image/delete/37
     * @param  idList
     * @return
     * @throws Exception
     */
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

    @Log("断点上传压缩文件")
    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
    @ResponseBody
    @CrossOrigin
    public CommonResponseDTO uploadFile(MultipartFileParam param, HttpServletRequest request){
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        CommonResponseDTO commonResponseDTO = null;

        if (isMultipart) {
            try {
                commonResponseDTO = (CommonResponseDTO) storageService.uploadFileByMappedByteBuffer(param); //上传

                return commonResponseDTO;
            } catch (IOException e) {
                e.printStackTrace();
                log.error("文件上传失败! {}", param.toString());

                return new CommonResponseDTO().fail().message("上传失败");
            }
        } else {
            return commonResponseDTO.fail().message("请选择文件上传");
        }
    }

    /**
     * 秒传判断，断点判断
     */
    @Log("秒传判断，断点判断")
    @RequestMapping(value = "checkFileMd5", method = RequestMethod.GET)
    @ResponseBody
    @CrossOrigin
    public Object checkFileMd5(String md5) throws IOException {
        CommonResponseDTO<ResultVo> commonResponseDTO = new CommonResponseDTO<>();

        Object processingObj = stringRedisTemplate.opsForHash()
                .get(Constants.FILE_UPLOAD_STATUS, md5);    //上传文件的状态

        if (processingObj == null) {    //该文件从未上传
//            return new ResultVo(ResultStatus.NO_HAVE);
            commonResponseDTO.setResultVo(new ResultVo(ResultStatus.NO_HAVE));
            commonResponseDTO.setMessage("文件未上传,马上上传");

            return commonResponseDTO;
        }

        String processingStr = processingObj.toString();
        boolean processing = Boolean.parseBoolean(processingStr);
        String value = stringRedisTemplate.opsForValue()
                .get(Constants.FILE_MD5_KEY + md5); //文件所在路径

        if (processing) {   //上传完成
            commonResponseDTO.setResultVo(new ResultVo(ResultStatus.IS_HAVE));
            commonResponseDTO.setMessage("文件已经上传");

            return commonResponseDTO;
        } else {    //上传未完成
            File confFile = new File(value);
            byte[] completeList = FileUtils.readFileToByteArray(confFile);
            List<String> missChunkList = new LinkedList<>();    //文件未上传的部分

            for (int i = 0; i < completeList.length; i++) {
                if (completeList[i] != Byte.MAX_VALUE) {
                    missChunkList.add(i + "");
                }
            }

            commonResponseDTO.setResultVo(new ResultVo(ResultStatus.ING_HAVE));
            commonResponseDTO.setMessage("文件上传一部分，开始断点续传");

            return commonResponseDTO;
        }
    }

    /*
    * 根据名称下载
    * */
    @Log("下载")
    @RequestMapping("/download")
    public CommonResponseDTO download(TileInfosDO tileInfosDO) throws IOException, MyException {
        storageService.download(tileInfosDO);

        return new CommonResponseDTO().success().message("下载完成");
    }

    /*
    * 根据名称查询
    * */
    @Log("根据名称查询")
    @RequestMapping(value = "getByName", method = RequestMethod.POST)
    @ResponseBody
    @CrossOrigin
    public CommonResponseDTO getByName(TileInfosDO tileInfosDO) throws IOException {
        String result = storageService.getByName(tileInfosDO);

        return new CommonResponseDTO().success().data(result);
    }

    /*
    * 查询所有文件
    * */
    @Log("查询所有文件")
    @GetMapping(value = "getAll")
    @CrossOrigin
    public CommonResponseDTO getAll(QueryRequest queryRequest) throws IOException {
        return new CommonResponseDTO().success().data(storageService.getAll(queryRequest));
    }
}
