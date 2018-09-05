package org.sourceforge.uptodater;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.*;


/**
 * Configuration wrapper around an UpToDater.  Use this wrapper if you are delivering
 * changes via a zip file and getting your db connection from a datasource.  Implementations
 * of this class make additional environmental accomodations.
 */
public abstract class UpToDateRunner  {

    /**
    * Support Objects
    */
    protected Logger logger;

    protected static final String UPTODATER_SESSION_INITIALIZATION_SQL_KEY = "uptodater.sessionInitializationSql";
    protected static final String IS_ACTIVE_KEY = "uptodater.active";
    protected static final String ZIP_NAME_KEY = "uptodater.override.zip";

    private ConfigData configData;

    /**
     * Default (no-args) Constructor
     */
    protected UpToDateRunner() {
        this(null);
    }

    protected UpToDateRunner(ConfigData configData) {
        logger = LoggerFactory.getLogger(getClass());
        this.configData = configData == null ? new ConfigData() : configData;
    }


    protected Connection getConnection(){
        InitialContext context;
        try {
            context =  new InitialContext();
        } catch (Exception e) {
            logger.error("Unable to get initial context: " + e.getMessage(), e);
            throw new ConfigurationException("Unable to get initial context: " + e.getMessage(), e);
        }
        String dataSourceName = getDatasourceName();
        if (dataSourceName == null){
            throw new ConfigurationException("The datasource name has not been set.");
        }
        try {
            return ((DataSource) context.lookup(dataSourceName)).getConnection();
        } catch (Exception e) {
            logger.error("Error getting database connection by name " + dataSourceName + ": " + e.getMessage(), e);
            throw new ConfigurationException("Error getting database connection by name " + dataSourceName + ": " + e.getMessage(), e);
        }
    }

    /**
     * The name of the datasource in JNDI.
     * @return the name to lookup in jndi
     */
    protected abstract String getDatasourceName();
    /**
     * The name of the update zip file.
     * @return the name of the resource
     */
    protected abstract String getUpDateZip();


    public abstract String getTableName();

    public String showUnappliedUpdates() throws Exception {
        Updater updater = new Updater(getTableName());
        try {
            Connection conn = getConnection();
            updater.initialize(conn, configData.get(UPTODATER_SESSION_INITIALIZATION_SQL_KEY, ""));
            List unapplied = updater.getUnappliedChanges();
            StringBuffer changeSummary = new StringBuffer();
            for (Iterator iterator = unapplied.iterator(); iterator.hasNext();) {
                DbChange change = (DbChange) iterator.next();
                changeSummary.append( change.getId() ).append(": ").append(change.getDescription()).append(" \t\t");
                changeSummary.append( change.getSqltext() );
                changeSummary.append("\n <br/>");
            }
            return changeSummary.toString();
        } catch (Exception e) {
            String eMessage = "Error getting unapplied update(s) from db " + e.getMessage();
            logger.error(eMessage, e);
            throw new ConfigurationException(eMessage, e);
        } finally {
            updater.close();
        }
    }

    public void applyUpdate(String updateId) throws Exception {
        Updater updater = new Updater(getTableName());
        try {
            Connection conn = getConnection();
            updater.initialize(conn, configData.get(UPTODATER_SESSION_INITIALIZATION_SQL_KEY, ""));
            List unapplied = updater.getUnappliedChanges();
            ArrayList changesToApply = new ArrayList();
            for (Iterator iterator = unapplied.iterator(); iterator.hasNext();) {
                DbChange change = (DbChange) iterator.next();
                if (change.getId().equals(updateId)) {
                    changesToApply.add(change);
                }
            }
            updater.executeChanges(changesToApply);
        } catch (Exception e) {
            String eMessage = "Error running update(s) from src " + getUpDateZip() + ": " + e.getClass().getName() + ": " + e.getMessage();
            logger.error(eMessage, e);
            throw new ConfigurationException(eMessage, e);
        } finally {
            updater.close();
        }
    }

    public void deleteUpdate(String updateId) throws Exception {
        Updater updater = new Updater(getTableName());
        try {
            Connection conn = getConnection();
            updater.initialize(conn, configData.get(UPTODATER_SESSION_INITIALIZATION_SQL_KEY, ""));
            updater.deleteChange(updateId);
        } catch (Exception e) {
            String eMessage = "Error deleting update(s) for id " + updateId + ": " + e.getClass().getName() + ": " + e.getMessage();
            logger.error(eMessage, e);
            throw new ConfigurationException(eMessage, e);
        } finally {
            updater.close();
        }
    }

    public void markChangeAsApplied(String updateId) throws Exception {
        Updater updater = new Updater(getTableName());
        try {
            Connection conn = getConnection();
            updater.initialize(conn, configData.get(UPTODATER_SESSION_INITIALIZATION_SQL_KEY, ""));
            updater.markAsApplied(updateId);
        } catch (Exception e) {
            String eMessage = "Error marking update as applied for id " + updateId + ": " + e.getClass().getName() + ": " + e.getMessage();
            logger.error(eMessage, e);
            throw new ConfigurationException(eMessage, e);
        } finally {
            updater.close();
        }
    }

    public String getRecentChanges(){
        Updater updater = new Updater(getTableName());
        Calendar since = GregorianCalendar.getInstance();
        since.add(Calendar.DAY_OF_YEAR, -30);
        try {
            Connection conn = getConnection();
            updater.initialize(conn, configData.get(UPTODATER_SESSION_INITIALIZATION_SQL_KEY, ""));
            List unapplied = updater.getChanges(since.getTime(), new Date());
            StringBuffer changeSummary = new StringBuffer();
            for (Iterator iterator = unapplied.iterator(); iterator.hasNext();) {
                DbChange change = (DbChange) iterator.next();
                changeSummary.append( change.getId() ).append(": ").append(change.getDescription()).append(" \t\t");
                changeSummary.append( change.getSqltext() ).append("\t\t");
                changeSummary.append( change.getCreated() );
                changeSummary.append( "/" );
                changeSummary.append( change.getAppliedDate() );
                changeSummary.append("\n <br/>");
            }
            return changeSummary.toString();
        } catch (Exception e) {
            String eMessage = "Error running update(s) from src " + getUpDateZip() + ": " + e.getClass().getName() + ": " + e.getMessage();
            logger.error(eMessage, e);
            throw new ConfigurationException(eMessage, e);
        } finally {
            updater.close();
        }
    }

    public boolean hasUnappliedUpdates() {
        return !doUpdate(true).isEmpty();
    }

    public void doUpdate()  {
        doUpdate(false);
    }

    public List<String> doUpdate(boolean dryRun) {
        List<String> appliedScriptNames = new ArrayList<String>();
        Updater updater = new Updater(getTableName());
        String zipfile = getZipOverride();
        if(StringUtils.isBlank(zipfile)) {
            zipfile = getUpDateZip();
        }
        if (zipfile == null){
            throw new ConfigurationException("The update zipfile name has not been set.");
        }
        InputStream is = getClass().getClassLoader().getResourceAsStream(zipfile);
        if (is == null) {
            throw new ConfigurationException(String.format("File not found in classpath: %s", zipfile));
        }
        SortedMap<String,String> scriptsMap;
        try {
            scriptsMap = new TreeMap<String,String>(Updater.loadScriptsFromZipFile(is));
        } catch (IOException e) {
            throw new ConfigurationException("Cannot load the zipfile of changes "+getUpDateZip(),e);
        }
        try {
                is.close();
            } catch(Exception e){
                // do nothing
            }
        Connection conn;
        try {
            int newScriptsPresent = 0;
            conn = getConnection();
            updater.initialize(conn, configData.get(UPTODATER_SESSION_INITIALIZATION_SQL_KEY, ""));
            boolean noUpdate = dryRun || isInactive();
            for(String source : scriptsMap.keySet()) {
                String contents =  scriptsMap.get(source);
                if (noUpdate && !updater.alreadyApplied(source)) {
                    logApplied(appliedScriptNames, source, contents);
                } else if (updater.update(source, contents)) {
                    logApplied(appliedScriptNames, source, contents);
                    newScriptsPresent++;
                }
            }
            logger.debug("" + newScriptsPresent + " db changes required.");
            updater.executeChanges(updater.getUnappliedChanges());

            if (isInactive()) {
                logger.info("UpToDater is NOT Enabled!");
            }
            logger.info((dryRun ? "Would have " : "") + "Executed " + newScriptsPresent+ " new scripts.");
        } catch (UpdateFailureException e) {
            logger.error("\n\nUpdate(s) " + e.getOriginalSql() + " failed \n\n", e);
            throw new ConfigurationException("Update(s) " + e.getOriginalSql() + " failed", e.getCause());
        } catch (Exception e) {
            String eMessage = "Error running update(s) from src " + getUpDateZip() + ": " + e.getClass().getName() + ": " + e.getMessage();
            logger.error(eMessage, e);
            throw new ConfigurationException(eMessage, e);
        } finally {
            updater.close();
        }
        return appliedScriptNames;
    }

    private void logApplied(List<String> appliedScriptNames, String source, String contents) {
        logger.info("Queuing change " + source + " for application.");
        logger.debug("Queuing change " + source + " for application - " + contents);
        appliedScriptNames.add(source);
    }

    /**
     * allows uptodater to be set as inactive, useful for multiple machines connecting to
     * same database
     * @return true by default if system is configured to run uptodater
     */
    protected boolean isInactive() {
        String value = configData.get(IS_ACTIVE_KEY, "true");
        return !Boolean.parseBoolean(value);
    }

    /**
     * @return filename if specified in over ride properties file, otherwise returns null
     */
    String getZipOverride() {
        String zipFile = configData.get(ZIP_NAME_KEY, "");
        return zipFile.equals("") ? null : zipFile;
    }


}
