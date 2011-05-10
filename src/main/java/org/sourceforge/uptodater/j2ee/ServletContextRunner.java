package org.sourceforge.uptodater.j2ee;

import org.sourceforge.uptodater.UpToDateRunner;
import org.sourceforge.uptodater.ConfigurationException;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletContext;

/**
 * Abstract base for servlet and application listener based implementations.
 */
public abstract class ServletContextRunner extends UpToDateRunner {
    public static final String DATASOURCE_NAME = "datasource";
    public static final String ZIPFILE_NAME = "zipfile";
    public static final String TABLE_NAME = "tablename";

    private String updateZip;
    private String dataSourceName;
    private String tablename;

    protected String getDatasourceName() {
        return dataSourceName;
    }

    protected String getUpDateZip() {
        return updateZip;
    }

    @Override
    public String getTableName() {
        return tablename;
    }

    public void doUpdate() {
        ServletContext ctx = getServletContext();
        dataSourceName = ctx.getInitParameter(DATASOURCE_NAME);
        updateZip = ctx.getInitParameter(ZIPFILE_NAME);
        tablename = ctx.getInitParameter(TABLE_NAME);
        if (StringUtils.isEmpty(dataSourceName)) {
            throw new ConfigurationException("You must provide an init value for " + DATASOURCE_NAME);
        }
        if (StringUtils.isEmpty(updateZip)) {
            throw new ConfigurationException("You must provide an init value for " + ZIPFILE_NAME);
        }
        if (StringUtils.isEmpty(tablename)) {
            logger.debug("Tablename was not configured, using default");
        }

        super.doUpdate();
    }


    abstract ServletContext getServletContext();
}
