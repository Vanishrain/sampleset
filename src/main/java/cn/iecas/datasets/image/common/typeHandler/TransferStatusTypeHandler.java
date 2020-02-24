package cn.iecas.datasets.image.common.typeHandler;

import cn.iecas.datasets.image.pojo.entity.uploadFile.TransferStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransferStatusTypeHandler extends BaseTypeHandler<TransferStatus> {
    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, TransferStatus transferStatus, JdbcType jdbcType) throws SQLException {
        preparedStatement.setInt(i,transferStatus.ordinal());

    }

    @Override
    public TransferStatus getNullableResult(ResultSet resultSet, String s) throws SQLException {
        int transferStatusValue = resultSet.getInt(s);
        if (0 == transferStatusValue)
            return TransferStatus.TRANSFERING;

        return TransferStatus.FINISHED;
    }

    @Override
    public TransferStatus getNullableResult(ResultSet resultSet, int i) throws SQLException {
        int transferStatusValue = resultSet.getInt(i);
        if (0 == transferStatusValue)
            return TransferStatus.TRANSFERING;

        return TransferStatus.FINISHED;
    }

    @Override
    public TransferStatus getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        int transferStatusValue = callableStatement.getInt(i);
        if (0 == transferStatusValue)
            return TransferStatus.TRANSFERING;

        return TransferStatus.FINISHED;
    }
}
