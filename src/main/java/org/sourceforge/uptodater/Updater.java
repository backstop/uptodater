package org.sourceforge.uptodater;

/* 
 * Created Date: Jan 10, 2005
 */

/**
 * @author rapruitt
 */

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * The core class.  You would normally use one of the runners to wrap around this class, but this class can be used
 * directly for special situations.
 * @see UpToDateRunner
 * @see org.sourceforge.uptodater.j2ee.UpToDaterServlet
 * @see org.sourceforge.uptodater.j2ee.UpdaterGeneric
 * @see org.sourceforge.uptodater.j2ee.jboss.JbossUpToDater
 * @see org.sourceforge.uptodater.j2ee.UpToDaterApplicationListener
 * @author rapruitt
 */
public class Updater {
    /**
     * The name of the table that will be used by default.
     */
    public static final String DEFAULT_TABLE_NAME = "uptodater";

    //Support objects.
    private static Logger logger = LoggerFactory.getLogger(Updater.class);

    private Connection conn;
    private Set<String> existingDescriptions = new HashSet<String>();
    private MessageDigest md;
    private Date now;
    private String tableName;


    public Updater(String tableName) {
        this.tableName = tableName;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new ConfigurationException("Cannot get an md5 algo", e);
        }
    }

    public Updater() {
        this(DEFAULT_TABLE_NAME);
    }

    /**
     * Take an InputStream that represents a zip file of the scripts that hold the changes, and produce a Map of the changes contained.
     *
     * @param is an InputStream that points to the Zip file.  Cannot be null.
     * @return a Map of filename -> file contents
     * @exception IOException if the stream cannot be read
     */
    public static Map<String,String> loadScriptsFromZipFile(InputStream is) throws IOException {
//        assert is != null;
        Map<String,String> changes = new HashMap<String,String>();

        ZipInputStream zipinp = new ZipInputStream(is);
        ZipEntry entry;
        while ((entry = zipinp.getNextEntry()) != null) {
            //System.out.println("UnZipping: " + entry);
            int count;
            byte databuff[] = new byte[2048];
            // write the files
            String fileName = entry.getName();
            ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
            while ((count = zipinp.read(databuff, 0, 2048)) != -1) {
                contentStream.write(databuff, 0, count);
            }
            contentStream.flush();
            contentStream.close();
            changes.put(fileName, contentStream.toString());
        }
        zipinp.close();
        return changes;
    }


    /**
     * Must be called prior to db operations; will load the existing commands.
     *
     * @param con the database connection to use
     * @param sessionInitializationSql any session initialization sql you want to run
     * @throws SQLException if there is a problem getting a connection the existing commands from the database
     */
    public void initialize(Connection con, String sessionInitializationSql) throws SQLException {
        this.conn = con;
        String sql = "Select description from "+tableName;

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                existingDescriptions.add(rs.getString("description"));
            }
        } finally {
            DBUtil.close(rs);
            DBUtil.close(pstmt);
        }

        initializeSession(sessionInitializationSql);

        now = new Date(System.currentTimeMillis());
    }

    private void initializeSession(String sessionInitializationSql) throws SQLException {
        if (StringUtils.isNotBlank(sessionInitializationSql)) {
            Statement stmt = null;
            try {
                stmt = conn.createStatement();
                stmt.execute(sessionInitializationSql);
            } finally {
                DBUtil.close(stmt);
            }
        }
    }

    /**
     * Will close out the database connection, please call when you are finished.
     */
    public void close() {
        DBUtil.close(conn);
        existingDescriptions.clear();
    }

    /**
     * Insert the script if it is not already present in the database. Does not execute the change, only stores it if it is not already present.
     * You must first call the initialize() method to set the connection.
     *
     * @param description the filename, or some other reasonable description for the change script
     * @param text        the actual script text
     * @return true if changes were inserted
     * @throws SQLException if there is an error on exec of the update
     * @throws IllegalStateException if the initialize() is not called
     */
    public boolean update(String description, String text) throws SQLException {
        return update(description, text, now);
    }

    /**
     * Insert the script if it is not already present in the database. Does not execute the change, only stores it if it is not already present.
     * You must first call the initialize() method to set the connection.
     *
     * @param description the filename, or some other reasonable description for the change script
     * @param text        the actual script text
     * @param insertDate  the date to store the change under
     * @return true if changes were inserted
     * @throws SQLException if there is an error on exec of the update
     * @throws IllegalStateException if the initialize() is not called
     */
    public boolean update(String description, String text, Date insertDate) throws SQLException {
        if (conn == null) {
            throw new IllegalStateException("You must first call initialize()!");
        }
//        assert description != null;
//        assert text != null;

        if (alreadyApplied(description)) {
            return false;
        }

        PreparedStatement pstmt = null;
        md.reset();
        md.update(text.getBytes());
        try {
            pstmt = conn.prepareStatement("insert into "+tableName+"(insert_date , sqltext, description, sqltext_hash) values(?, ?,  ?, ?) ");
            pstmt.setDate(1, insertDate);
            pstmt.setString(2, text);
            pstmt.setString(3, description);
            pstmt.setString(4, new String(md.digest()));
            pstmt.executeUpdate();
        } finally {
            DBUtil.close(pstmt);
        }
        existingDescriptions.add(description);
        return true;
    }

    boolean alreadyApplied(String description) {
        if (existingDescriptions.contains(description)) {
            return true;
        }

        // Some databases - eg sqlserver - have odd case sensitivities;
        // this check should not be necessary
        for (String cmd : existingDescriptions) {
            if (cmd.equalsIgnoreCase(description)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Execute any changes that are in the ddlchanges table and are as yet unapplied. If any changes fail, ALL changes will be rolled back.
     *
     * @return how many changes were run
     * @throws UpdateFailureException if any of the changes fail
     * @throws SQLException           if there is problem with something besides the proposed updates
     * @throws IllegalStateException  if the initialize() is not called
     */
    public int executeChanges(List<DbChange> changes) throws SQLException, UpdateFailureException {
        if (conn == null) {
            throw new IllegalStateException("You must first call initialize()!");
        }
        if (changes.isEmpty()) {
            return 0;
        }

        ResultSet rs = null;
        boolean mustResetToAutocommit = false;
        if (conn.getAutoCommit()) {
            mustResetToAutocommit = true;
            conn.setAutoCommit(false);
        }

        int executedChanges;
        boolean shouldCommit = false;
        try {
            executedChanges = runDBChanges(changes);
            shouldCommit = true;
        } finally {
            try {
                if (shouldCommit){
                    conn.commit();
                } else {
                    try {
                        conn.rollback();
                    } catch(Exception e){
                        logger.warn("Exception while rolling back: "+e.getMessage(), e);
                    }
                }
            } finally {
                DBUtil.close(rs);
            }

        }

        if (mustResetToAutocommit) {
            try {
                conn.setAutoCommit(true);
            } catch (Exception e) {
                logger.warn("Trouble reseting to autocommit: " + e.getMessage(), e);
            }
        }
        return executedChanges;
    }

    private int runDBChanges(List<DbChange> changes) throws UpdateFailureException, SQLException {
        PreparedStatement executedStmtUpdate = null;
        try {
            int executedChanges = 0;
            StatementPreparer statementPreparer = new StatementPreparer(new ConfigData());
            Date currentDate = new Date(System.currentTimeMillis());
            for (DbChange change : changes) {
                executedStmtUpdate = conn.prepareStatement("update " + tableName + " set applied_date = ? where change_id = ?");
                executedStmtUpdate.setDate(1, currentDate);
                String currentSqlText = null;
                try {
                    for (String sqlText : change.getSqlChanges()) {
                        currentSqlText = sqlText;
                        logger.debug("Executing change " + sqlText);
                        ChangeExecutor.createChangeExecutor(statementPreparer.prepare(sqlText)).execute(conn);
                        logger.debug("Executed change");
                    }
                } catch (SQLException e) {
                    if(change.isOptional()) {
                        logger.warn("An error occurred trying to execute the following OPTIONAL database change:\n" + currentSqlText + "\n Please make sure this change failed as expected",e);
                    } else {
                        throw new UpdateFailureException(e.getMessage(), currentSqlText, e);
                    }
                }
                executedStmtUpdate.setString(2, change.getId());
                executedStmtUpdate.executeUpdate();
                executedChanges++;
                executedStmtUpdate.close();
            }
            return executedChanges;
        } finally {
            DBUtil.close(executedStmtUpdate);
        }
    }

    /**
     * A List of DbChanges that are currently pending.
     *
     * @return List of DbChange objects
     * @see DbChange
     * @throws java.sql.SQLException if there is an error
     */
    public List<DbChange> getUnappliedChanges() throws SQLException {
        List<DbChange> unappliedChanges = new ArrayList<DbChange>();
        if (conn == null) {
            throw new IllegalStateException("You must first call initialize()!");
        }
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "Select change_id, sqltext, description, insert_date from "+tableName+" where applied_date is null order by insert_date, description";
            pstmt = conn.prepareStatement(sql);

            rs =pstmt.executeQuery();
            while (rs.next()) {
                String id = rs.getString("change_id");
                DbChange change = new DbChange(id, rs.getString("sqltext"), rs.getDate("insert_date"), rs.getString("description"), null);
                unappliedChanges.add(change);
            }
        } finally {
            DBUtil.closeAll(null, pstmt, rs);
        }
        return unappliedChanges;
    }

    public void deleteChange(String changeId) throws SQLException {
        if (conn == null) {
            throw new IllegalStateException("You must first call initialize()!");
        }
        PreparedStatement pstmt = null;
        try {
            String sql = "delete from from "+tableName+" where change_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, changeId);
            pstmt.executeUpdate();
        } finally {
            DBUtil.close(pstmt);
        }
    }

    /**
     * Get the name of the table that tracks the changes.
     * @return the table name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Get any changes created in the indicated range.
     * @param earliest on or after this date
     * @param latest before or on this date
     * @return a List of DbChanges
     * @throws java.sql.SQLException if something goes wrong
     */
    public List<DbChange> getChanges(java.util.Date earliest, java.util.Date latest) throws SQLException {
        List<DbChange> changes = new ArrayList<DbChange>();
        if (conn == null) {
            throw new IllegalStateException("You must first call initialize()!");
        }
        PreparedStatement pstmt = null;

        try {
            String sql = "Select change_id, sqltext, description, insert_date, applied_date from "
                    +tableName+" where insert_date >= ? and insert_date <= ?  order by insert_date, description";
            pstmt = conn.prepareStatement(sql);
            pstmt.setDate(1, new Date(earliest.getTime()));
            pstmt.setDate(2, new Date(latest.getTime()));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String id = rs.getString("change_id");
                DbChange change = new DbChange(id, rs.getString("sqltext"), rs.getDate("insert_date"),
                        rs.getString("description"), rs.getDate("applied_date"));
                changes.add(change);
            }
        } finally {
            DBUtil.close(pstmt);
        }
        return changes;
    }

    public void markAsApplied(String updateId) throws SQLException {
        PreparedStatement pstmt = null;
        try {
            String sql = "update " +tableName+" set applied_date = ? where change_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setDate(1, new java.sql.Date(new java.util.Date().getTime()));
            pstmt.setString(2, updateId);
            int changeCount = pstmt.executeUpdate();
            assert changeCount > 0;
        } finally {
            DBUtil.close(pstmt);
        }
    }
}
