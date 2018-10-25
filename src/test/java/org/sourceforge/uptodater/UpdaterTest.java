package org.sourceforge.uptodater;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class UpdaterTest {

    private static String TABLE_NAME = "uptodater";
    private static Connection connection;

    @BeforeClass
    public static void setUp() throws Exception {
        Properties properties = new Properties();
        properties.load(UpdaterTest.class.getResourceAsStream("/UpToDater.properties"));

        Class.forName(properties.getProperty("jdbc.driverClassName"));
        connection = DriverManager.getConnection(properties.getProperty("jdbc.url"));
        createTestTable();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        try {
            dropTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Connection getConnection() {
        return connection;
    }

    private static void createTestTable() throws SQLException {
        String sql = "create table " + TABLE_NAME + " (\n" +
                "    sqltext_hash varchar(100) primary key not null,\n" +
                "    insert_date datetime not null default getdate(),\n" +
                "    description varchar(250) not null,\n" +
                "    sqltext text,\n" +
                "    change_id int auto_increment,\n" +
                "    applied_date datetime\n" +
                ");";

        Connection conn;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            stmt.execute(sql);
        } finally {
            DBUtil.close(stmt);
        }
    }

    private static void dropTable() throws Exception{
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            stmt.execute("drop table " + TABLE_NAME + ";");
        } finally {
            DBUtil.closeAll(conn, stmt);
        }
    }

    @Test
    public void doNotUpdateSameDescription() throws SQLException {
        Updater updater = new Updater(TABLE_NAME);
        updater.initialize(getConnection(), "");
        String createTable = "create table test_table (\n"
                + "  id int auto_increment not null\n"
                + ")";
        updater.update("file1.sql", createTable);
        updater.update("file1.sql", createTable + " -- and a slight change");
        assertEquals(1, updater.getUnappliedChanges().size());
    }

    @Test
    public void doNotUpdateSameContents() throws SQLException {
        Updater updater = new Updater(TABLE_NAME);
        updater.initialize(getConnection(), "");
        String createTable = "create table test_table (\n"
                + "  id int auto_increment not null\n"
                + ")";
        updater.update("file1.sql", createTable);
        updater.update("file2.sql", createTable);
        assertEquals(1, updater.getUnappliedChanges().size());
    }
}