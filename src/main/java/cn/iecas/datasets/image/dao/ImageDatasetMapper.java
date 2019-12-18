package cn.iecas.datasets.image.dao;

import cn.iecas.datasets.image.pojo.domain.ImageDataSetInfoDO;
import cn.iecas.datasets.image.pojo.dto.ImageDataSetInfoRequestDTO;
import cn.iecas.datasets.image.pojo.entity.Statistic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ImageDatasetMapper  extends BaseMapper<ImageDataSetInfoDO> {
    List<Statistic> getStatistic();
    ImageDataSetInfoDO getImageDataSetById(int id);
    void updateNumber(ImageDataSetInfoDO imageDataSetInfoDO);
    void insertDataset(ImageDataSetInfoDO imageDataSetInfoDO);
    void updateImageDataset(int value,int imagesetid,String field);
    List<ImageDataSetInfoDO> listImageDataSetInfos(Page page, @Param("imageDatasetInfoRequestDTO") ImageDataSetInfoRequestDTO imageDatasetInfoRequestDTO);

}
