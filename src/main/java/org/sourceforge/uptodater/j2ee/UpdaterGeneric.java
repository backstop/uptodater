package org.sourceforge.uptodater.j2ee;

import org.sourceforge.uptodater.Updater;
import org.sourceforge.uptodater.UpToDateRunner;

/*
 * Created Date: Mar 29, 2005
 */

/**
 * An MBean implementation of uptodater.
 * @see org.sourceforge.uptodater.j2ee.jboss.JbossUpToDater a jboss specific subclass
 * @author rapruitt
 */
public abstract class UpdaterGeneric extends UpToDateRunner implements UpdaterGenericMBean {

    protected String updateZip;
    protected String datasourceName;
    protected String tableName = Updater.DEFAULT_TABLE_NAME;

    /**
     * Default (no-args) Constructor
     */
    public UpdaterGeneric() {
    }

    /**
     * @jmx.managed-attribute
     */
    public String getUpDateZip() {
        return updateZip;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setUpDateZip(String updateZip) {
        this.updateZip = updateZip;
    }


    // java:jdbc/YourPool

    /**
     * @jmx.managed-attribute
     */
    public String getDatasourceName() {
        return datasourceName;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setDatasourceName(String datasourceJndiName) {
        datasourceName = datasourceJndiName;
    }

    /**
     * @jmx.managed-attribute
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void start() throws Exception {
        doUpdate();
    }

    public void init() throws Exception {
    }

    public void stop() throws Exception {
    }

    public void destroy() throws Exception {
    }
}
