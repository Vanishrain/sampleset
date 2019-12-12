package cn.iecas.datasets.image.common.domain;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

@Data
public class QueryRequest {
    private int id;
    private int pageNo = 1;
    private int pageSize = 10;
}
