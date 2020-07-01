package cn.iecas.sampleset.common.domain;

import lombok.Data;

@Data
public class QueryRequest {
    private int id;
    private int pageNo = 1;
    private int pageSize = 10;
}
