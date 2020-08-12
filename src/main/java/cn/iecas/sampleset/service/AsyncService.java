package cn.iecas.sampleset.service;

import cn.iecas.sampleset.pojo.domain.SampleSetInfo;

public interface AsyncService {
    void decompressAndStorageSampleSet(SampleSetInfo sampleSetInfo, String md5, String uploadFilePath) throws Exception;
}
