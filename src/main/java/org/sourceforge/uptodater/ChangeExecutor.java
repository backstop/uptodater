package org.sourceforge.uptodater;

import java.sql.*;

public abstract class ChangeExecutor {

    protected String sqlText;
    protected Connection connection;

    private ChangeExecutor(String sqlText) {
        this.sqlText = sqlText;
    }

    public final void execute(Connection con) throws SQLException {
        this.connection = con;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = createStatement();
            preparedStatement.executeUpdate();
        } finally {
            DBUtil.close(preparedStatement);
        }
    }


    abstract PreparedStatement createStatement() throws SQLException;

    public static ChangeExecutor createChangeExecutor(final String sqlText) {
        String lowerCaseSqlText = sqlText.trim().toLowerCase();
        if(lowerCaseSqlText.startsWith("call")) {
            return new CallableChangeExecutor(sqlText);
        } else {
            return new StatementChangeExecutor(sqlText);
        }
    }

    public static class CallableChangeExecutor extends ChangeExecutor {

        private CallableChangeExecutor(String sqlText) {
            super(sqlText);
        }
        public PreparedStatement createStatement() throws SQLException {
            return connection.prepareCall(sqlText);
        }
    }

    public static class StatementChangeExecutor extends ChangeExecutor {
        private StatementChangeExecutor (String sqlText) {
            super(sqlText);
        }
        public PreparedStatement createStatement() throws SQLException {
            return connection.prepareStatement(sqlText);
        }
    }
}
