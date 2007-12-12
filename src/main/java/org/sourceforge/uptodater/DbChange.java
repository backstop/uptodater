package org.sourceforge.uptodater;

import java.util.*;
import java.util.regex.Pattern;

/* 
 * Created Date: Jan 10, 2005
 */

/**
 * @author rapruitt
 */
public class DbChange implements Comparable{
    private String sqltext;
    private Date created;
    private String description;
    private Date appliedDate;
    private String id;
    private List<String> sqlChanges = null;

    private Map<String,String> configData = null;

    private static final String META_PREFIX = "uptodater.";
    private static final String OPTIONAL_PARAMETER = META_PREFIX + "optional";
    private static final String STATEMENT_SEPARATOR_PARAMETER = META_PREFIX + "statement.separator";

    public DbChange(String id, String sqltext, Date created, String description, Date appliedDate) {
        this.sqltext = sqltext;
        this.created = created;
        this.id = id;
        this.description = description;
        this.appliedDate = appliedDate;
        initializeConfigData();
    }

    public String getSqltext() {
        return sqltext;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(Date appliedDate) {
        this.appliedDate = appliedDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public int compareTo(Object o) {
        DbChange other = (DbChange) o;
        if (0 != this.created.compareTo(other.created)){
            return this.created.compareTo(other.created);
        } else {
            return this.description.compareTo(other.description);
        }
    }

    protected List<String> getSqlChanges() {
        String statementSeparator = ";";
        if (configData.containsKey(STATEMENT_SEPARATOR_PARAMETER))
            statementSeparator = configData.get(STATEMENT_SEPARATOR_PARAMETER);
        Pattern splitPattern = Pattern.compile(statementSeparator, Pattern.MULTILINE);
        if (sqlChanges == null) {
            sqlChanges = new ArrayList<String>();
            for(String change : splitPattern.split(getSqltext())) {
                change = change.trim();
                if(change.length() > 0) {
                    sqlChanges.add(change);
                }
            }
        }
        return sqlChanges;
    }

    public boolean isOptional() {
        if(!configData.containsKey(OPTIONAL_PARAMETER)) {
            return false;
        }
        String value = configData.get(OPTIONAL_PARAMETER);
        return Boolean.parseBoolean(value);
    }

    private void initializeConfigData() {
        configData = new HashMap<String,String>();
        if(sqltext == null)
            return;
        String [] lines = sqltext.trim().split("\n");
        for(String line : lines) {
            line = line.trim();
            if(line.startsWith("--") && line.contains(META_PREFIX)) {
                String[] nameValue = line.split("--")[1].split("=");
                if(nameValue.length > 1) {
                    String name = nameValue[0].trim();
                    String value = nameValue[1].trim();
                    configData.put(name, value);
                }
            }
        }
    }
}
