package org.sourceforge.uptodater;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Simple utility methods
 */
class DBUtil {
    private DBUtil() {
    }

    static void closeAll(Connection conn, Statement stmt, ResultSet rs){
        close(rs);
        closeAll(conn, stmt);
    }

    static void closeAll(Connection conn, Statement stmt){
        close(stmt);
        close(conn);
    }

    static void close(Connection conn){
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            //ignore
        }
    }

    static void close(Statement stmt){
        try {
            if(stmt != null ) {
                stmt.close();
            }
        } catch (SQLException e) {
            // ignore
        }
    }
    static void close(ResultSet rs){
        try {
            if(rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            // ignore
        }
    }
}
