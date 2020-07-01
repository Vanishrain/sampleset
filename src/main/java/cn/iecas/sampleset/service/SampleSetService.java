package cn.iecas.sampleset.service;


import cn.iecas.sampleset.pojo.domain.SampleSetInfo;
import cn.iecas.sampleset.pojo.dto.SampleSetCreationInfo;
import cn.iecas.sampleset.pojo.dto.common.PageResult;
import cn.iecas.sampleset.pojo.dto.SampleSetInfoRequestParam;
import cn.iecas.sampleset.pojo.dto.SampleSetStatistic;
import cn.iecas.sampleset.pojo.enums.OperationType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;
import java.util.List;

/**
 * @author vanishrain
 */
public interface SampleSetService extends IService<SampleSetInfo> {

    /**
     * 获取影像数据集的统计信息
     */
    SampleSetStatistic getStatistic();


    /**
     * 更新样本集的数量信息
     * @param sampleSetId
     */
    void updateSampleSetCount(int sampleSetId, int count, OperationType operationType);


    /**
     * 获取影像数据集详细信息
     * @param sampleSetInfoRequestParam
     * @return
     */
    PageResult<SampleSetInfo> listSampleSetInfos(SampleSetInfoRequestParam sampleSetInfoRequestParam);

    /**
     * 根据id删除对应数据集，同时会删除数据
     * @param imageDataSetId
     */
    void deleteSampleSetById(int imageDataSetId) throws Exception;

    /**
     * 数据集调用 创建样本集接口
     * @param sampleSetCreationInfo 样本集创建相关信息
     */
    void createSampleSet(SampleSetCreationInfo sampleSetCreationInfo) throws IOException;

    /**
     * 删除image_info中的数据集信息，同时删除tile_info表中对应数据集id的切片数据
     * @param sampleSetIdList 样本集id列表
     */
    void deleteSampleSetByIds(List<Integer> sampleSetIdList) throws Exception;


}
