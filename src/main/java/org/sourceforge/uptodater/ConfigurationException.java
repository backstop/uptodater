package org.sourceforge.uptodater;

public class ConfigurationException extends RuntimeException {
    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable caus) {
        super(message, caus);
    }
}
