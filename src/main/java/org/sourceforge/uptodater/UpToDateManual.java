package org.sourceforge.uptodater;



import junit.framework.TestSuite;
import junit.framework.TestCase;
import org.apache.commons.lang.RandomStringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.text.SimpleDateFormat;


/**
 * @author rapruitt
 */

public class UpToDateManual extends TestCase {
    private String zipFile = "updates.zip";
    ResourceBundle bundle = ResourceBundle.getBundle("UpToDater");

    String tableName = "ddlchanges_test";

    public UpToDateManual(String str) {
        super(str);
    }

    public static TestSuite suite() {
        return new TestSuite(UpToDateManual.class);
    }

    protected Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName(bundle.getString("default.driver-class"));
        return DriverManager.getConnection(bundle.getString("default.url"),
                bundle.getString("default.user"), bundle.getString("default.password"));
    }

    public void tearDown() throws Exception {
        super.tearDown();
        dropTable();
    }
    public void setUp() throws Exception {
        super.setUp();
        createTestTable();
    }

    private void createTestTable() throws SQLException, ClassNotFoundException {
        String sql = "create table "+tableName+" (\n" +
                "    sqltext_hash varchar(100) primary key not null,\n" +
                "    insert_date datetime not null default getdate(),\n" +
                "    description varchar(250) not null,\n" +
                "    sqltext text,\n" +
                "    change_id int identity,\n" +
                "    applied_date datetime\n" +
                ");";

        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            stmt.execute(sql);
        } finally {
            DBUtil.closeAll(conn,stmt);
        }
    }

    void dropTable() throws Exception{
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            stmt.execute("drop table "+tableName +" ; ");
        } finally {
            DBUtil.closeAll(conn,stmt);
        }
    }

    public void tdestReadFiles() throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(zipFile);
        assertNotNull(is);
        Map changes = Updater.loadScriptsFromZipFile(is);

        assertNotNull(changes);
        assertTrue(changes.size() > 1);
    }


    public void testGood() throws Exception {
        Updater updater = new Updater(tableName);
        Map changes = new HashMap(1);

        // For now, make a fake update for testing
        changes.clear();
        String random = "UpToDaterTest-" + RandomStringUtils.randomAlphabetic(10);
        changes.put( random, "update "+tableName+" set insert_date = '12/13/1970' where description = '"+random+"'");

        Connection conn;
        try {
            conn = getConnection();
            updater.initialize(conn, "");
            for (Iterator iterator = changes.keySet().iterator(); iterator.hasNext();) {
                String desc = (String) iterator.next();
                boolean updateRequired = updater.update(desc, (String) changes.get(desc));
                assertTrue(updateRequired);
            }
            int updateCount = updater.executeChanges(updater.getUnappliedChanges());
            assertEquals(1, updateCount);
        } finally {
            updater.close();
        }

    }

    public void testDupsFail() throws Exception {
        Updater updater = new Updater(tableName);
        String random = "UpToDaterTest-" + RandomStringUtils.randomAlphabetic(10);
        // For now, make a fake update for testing
        String source1 = random+"test-1";
        String source2 = random+"test-2";

        String sql = "update "+tableName+" set insert_date = '12/13/1970' where description = '" + random + "'";


        Connection conn;
        try {
            conn = getConnection();
            updater.initialize(conn, "");
            boolean updateRequired = updater.update( source1, sql);
            assertTrue(updateRequired);
            updateRequired = updater.update( source2, sql);
            assertFalse(updateRequired);
        } finally {
            updater.close();
        }
    }

    public void testCorrectExecutionOrder() throws Exception {
        Updater updater = new Updater(tableName);

        // Three updates, each affecting the same set of rows; they should be sorted by filename
        String random ="UpToDaterTest-" + RandomStringUtils.randomAlphabetic(10);
        String source1 = random+"test-2";
        String source2 = random+"test-3";
        String source3 = random+"test-1";
        String sql1 = "update "+tableName+" set insert_date = '12/13/1971'  where description like '"+ random + "%'";
        String sql2 = "update "+tableName+" set insert_date = '12/13/1972' where description like '"+ random + "%'";
        String sql3 = "update "+tableName+" set insert_date = '12/13/1973'  where description like '"+ random + "%'";

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        java.util.Date dateShouldBe = sdf.parse("12/13/1972");

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            updater.initialize(conn, "");
            boolean updateRequired = updater.update( source1, sql1);
            assertTrue(updateRequired);
            updateRequired = updater.update( source2, sql2);
            assertTrue(updateRequired);
            updateRequired = updater.update( source3, sql3);
            assertTrue(updateRequired);
            updater.executeChanges(updater.getUnappliedChanges());
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select insert_date  from "+tableName+" where description like '"+random+"%'");
            assertTrue(rs.next());
            Date insertDate = rs.getDate(1);
            assertEquals(dateShouldBe, new Date(insertDate.getTime())  );
        } finally {
            DBUtil.closeAll(conn,stmt,rs);
        }
    }

    public void testBad() throws Exception {
        Updater updater = new Updater(tableName);
        Map changes = new HashMap();

        // For now, make a fake update for testing
        changes.clear();
        String name = "UpToDaterTest-" + RandomStringUtils.randomAlphabetic(10);
        String sql = "update ddlchangezzzs set insert_date = '12/13/1972' where description = '" + name + "'";
        changes.put(name, sql);
        try {
        Connection conn = getConnection();
        updater.initialize(conn, "");
        for (Iterator iterator = changes.keySet().iterator(); iterator.hasNext();) {
            String desc = (String) iterator.next();
            boolean updateRequired = updater.update(desc, (String) changes.get(desc));
            assertTrue(updateRequired);
        }
        int updateCount = 0;
        try {
            updateCount = updater.executeChanges(updater.getUnappliedChanges());
            fail("An exception should have been thrown.");
        } catch (UpdateFailureException updateFailure) {
            // good
            assertEquals(sql, updateFailure.getOriginalSql()) ;
        }
        assertEquals(0, updateCount);
        } finally {
            updater.close();
        }

    }


}
