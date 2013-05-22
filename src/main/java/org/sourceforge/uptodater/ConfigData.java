package org.sourceforge.uptodater;

import java.util.*;

public class ConfigData {

    private static final String DEFAULT_CONFIGURATION_NAME = "uptodater";

    protected Map<String,String> configuration = new HashMap<String,String>();

    public ConfigData() {
        addOverrideConfiguration(DEFAULT_CONFIGURATION_NAME);
    }

    public void addOverrideConfiguration(String configurationName) {
        try {
            ResourceBundle res = ResourceBundle.getBundle(configurationName);
            Enumeration<String> keys = res.getKeys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                configuration.put(key, res.getString(key));
            }
        } catch (MissingResourceException e) {
            //gulp
        }
    }


    public String get(final String propertyName, final String defaultValue) {
        String key = propertyName.trim();
        if(System.getProperties().containsKey(propertyName)) {
            return System.getProperty(propertyName);
        } else if(configuration.containsKey(key)) {
            return configuration.get(key);
        }
        else {
            return defaultValue;
        }
    }

}
