package org.sourceforge.uptodater;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

public class DBUtilTest extends MockObjectTestCase {
    public void testNullConnection() throws Exception {
        Connection connection = null;
        DBUtil.close(connection);
    }

    public void testNullResultSet() throws Exception {
        ResultSet resultSet = null;
        DBUtil.close(resultSet);
    }

    public void testNullPreparedStatement() throws Exception {
        PreparedStatement preparedStatement = null;
        DBUtil.close(preparedStatement);
    }

    public void testClosingConnection() throws Exception {
        Mock mockConnection = mock(Connection.class);
        mockConnection.expects(once()).method("close");
        Connection connection = (Connection) mockConnection.proxy();
        DBUtil.close(connection);
    }

    public void testClosingResultSet() throws Exception {
        Mock mockResultSet = mock(ResultSet.class);
        mockResultSet.expects(once()).method("close");
        ResultSet resultSet = (ResultSet) mockResultSet.proxy();
        DBUtil.close(resultSet);
    }

    public void testClosingPreparedStatement() throws Exception {
         Mock mockPreparedstatement = mock(PreparedStatement.class);
        mockPreparedstatement.expects(once()).method("close");
        PreparedStatement preparedstatement = (PreparedStatement) mockPreparedstatement.proxy();
        DBUtil.close(preparedstatement);
    }
}
