package cn.iecas.sampleset.common.typeHandler;

import cn.iecas.sampleset.pojo.enums.SampleSetStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SampleSetStatusTypeHandler extends BaseTypeHandler<SampleSetStatus> {
    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, SampleSetStatus sampleSetStatus, JdbcType jdbcType) throws SQLException {
        preparedStatement.setInt(i,sampleSetStatus.getValue());
    }

    @Override
    public SampleSetStatus getNullableResult(ResultSet resultSet, String s) throws SQLException {
        switch (resultSet.getInt(s)){
            case 0: return SampleSetStatus.CREATING;
            case 1: return SampleSetStatus.TOUPLOAD;
            default:return SampleSetStatus.FINISH;
        }

    }

    @Override
    public SampleSetStatus getNullableResult(ResultSet resultSet, int i) throws SQLException {
        switch (resultSet.getInt(i)){
            case 0: return SampleSetStatus.CREATING;
            case 1: return SampleSetStatus.TOUPLOAD;
            default:return SampleSetStatus.FINISH;
        }
    }

    @Override
    public SampleSetStatus getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        switch (callableStatement.getInt(i)){
            case 0: return SampleSetStatus.CREATING;
            case 1: return SampleSetStatus.TOUPLOAD;
            default:return SampleSetStatus.FINISH;
        }
    }
}
