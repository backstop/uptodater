package org.sourceforge.uptodater;

/* 
 * Created Date: Jan 10, 2005
 */

/**
 * @author rapruitt
 */
public class UpdateFailureException extends Exception {
    private String origSql;

    public UpdateFailureException(String message, String originalSql, Throwable caus) {
        super(message, caus);
        origSql = originalSql;
    }

    /**
     * The sql statement that failed.
     * @return the text of the statement
     */
    public String getOriginalSql(){
        return origSql;
    }

    public String getMessage() {
        return super.getMessage() + " while executing " + origSql;
    }
}
