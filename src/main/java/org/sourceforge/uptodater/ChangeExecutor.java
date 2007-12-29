package org.sourceforge.uptodater;

import java.sql.*;

public abstract class ChangeExecutor {

    public static interface DeferredStatement {
        void doIt() throws SQLException;
        Statement getStatement();
    }

    protected String sqlText;
    protected Connection connection;

    private ChangeExecutor(String sqlText) {
        this.sqlText = sqlText;
    }

    public final void execute(Connection con) throws SQLException {
        this.connection = con;
        DeferredStatement deferredStatement = null;
        try {
            deferredStatement = createDeferredStatement();
            deferredStatement.doIt();
        } finally {
            DBUtil.close(deferredStatement.getStatement());
        }
    }


    abstract DeferredStatement createDeferredStatement() throws SQLException;


    public static ChangeExecutor createChangeExecutor(final String sqlText) {
        String lowerCaseSqlText = sqlText.trim().toLowerCase();
        if(lowerCaseSqlText.startsWith("call")) {
            return new CallableChangeExecutor(sqlText);
        } else {
            return new StatementChangeExecutor(sqlText);
        }
    }

    public static class CallableChangeExecutor extends ChangeExecutor {
        CallableStatement stmt;

        private CallableChangeExecutor(String sqlText) {
            super(sqlText);
        }
        public DeferredStatement createDeferredStatement() throws SQLException {
            stmt = connection.prepareCall(sqlText);
            return new DeferredStatement() {

                public void doIt() throws SQLException {
                    stmt.execute();
                }

                public Statement getStatement() {
                    return stmt;
                }
            };
        }
    }

    public static class StatementChangeExecutor extends ChangeExecutor {
        Statement stmt;

        private StatementChangeExecutor (String sqlText) {
            super(sqlText);
        }

        public DeferredStatement createDeferredStatement() throws SQLException {
            stmt = connection.createStatement();
            return new DeferredStatement() {

                public void doIt() throws SQLException {
                    stmt.executeUpdate(sqlText);
                }

                public Statement getStatement() {
                    return stmt;
                }
            };
        }
    }
}
