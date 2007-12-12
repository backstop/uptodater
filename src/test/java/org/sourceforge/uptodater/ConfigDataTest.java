package org.sourceforge.uptodater;

import junit.framework.TestCase;

public class ConfigDataTest extends TestCase {

    public void testGet() throws Exception {
        final String testKey = "testKey";
        final String testValue = "testValue";
        ConfigData myConfigData = new ConfigData() {
            public void addOverrideConfiguration(String configurationName) {
                configuration.put(testKey, testValue);
            }
        };

        assertEquals(myConfigData.get(testKey, null), testValue);
        assertEquals(myConfigData.get(testKey + "  ", null), testValue);
        assertEquals(myConfigData.get("NOTFOUND", ""), "");
        assertNull(myConfigData.get("NOTFOUND", null));

    }
}
