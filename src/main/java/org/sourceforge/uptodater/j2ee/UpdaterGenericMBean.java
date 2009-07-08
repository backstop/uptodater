package org.sourceforge.uptodater.j2ee;

/* 
 * Created Date: Mar 29, 2005
 */


/**
 * @author rapruitt
 */
public interface UpdaterGenericMBean {
    String getUpDateZip();
    void setUpDateZip(String updateZip);
    String getDatasourceName();
    void setDatasourceName(String name);
    String getTableName();
    void setTableName(String nameOfTable);
    Boolean getIsActive();

    void start() throws Exception;
    void init() throws Exception;
    void stop() throws Exception;
    void destroy() throws Exception;

    /**
     * Show any unapplied (failed) updates.
     * @return a displayable description
     * @throws Exception
     */
    String showUnappliedUpdates() throws Exception;

    /**
     * Apply a particular update.
     * @param updateId
     * @throws Exception
     */
    void applyUpdate(String updateId) throws Exception;

    /**
     * Delete a particular update.
     * @param updateId
     * @throws Exception
     */
    void deleteUpdate(String updateId) throws Exception;

    /**
     * Get any changes to the system in the last 30 days.
     * @return a displayable description
     * @throws Exception
     */
    String getRecentChanges() throws Exception;

    /**
     * Mark an individual update as already having been applied.  This will mean that updater will ignore it in the future.
     * @param updateId
     * @throws Exception
     */
    void markChangeAsApplied(String updateId) throws Exception;
}
