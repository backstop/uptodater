package org.sourceforge.uptodater;

/* 
 * Created Date: Jan 10, 2005
 */

/**
 * @author rapruitt
 */
public class ConfigurationException extends RuntimeException {
    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable caus) {
        super(message, caus);
    }
}
