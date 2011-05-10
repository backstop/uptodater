package org.sourceforge.uptodater;

import junit.framework.TestCase;
import org.sourceforge.uptodater.j2ee.UpdaterGeneric;

public class UpToDateRunnerTest extends TestCase {

    public void testIsInActive() throws Exception {
        ConfigData configData = new ConfigData()  {
            public void addOverrideConfiguration(String configurationName) {

            }
        };
        UpToDateRunnerInstance runner = new UpToDateRunnerInstance(configData);
        assertFalse(runner.isInactive());

        configData.configuration.put(UpToDateRunner.IS_ACTIVE_KEY, "false");
        runner = new UpToDateRunnerInstance(configData);
        assertTrue(runner.isInactive());

        configData.configuration.remove(UpToDateRunner.IS_ACTIVE_KEY);
        configData.configuration.put(UpToDateRunner.IS_ACTIVE_KEY, "true");
        runner = new UpToDateRunnerInstance(configData);
        assertFalse(runner.isInactive());

    }

    private static class UpToDateRunnerInstance extends UpToDateRunner {

        UpToDateRunnerInstance (ConfigData configData) {
            super(configData);
        }
        protected String getDatasourceName() {
            return null;
        }

        protected String getUpDateZip() {
            return null;
        }

        @Override
        public String getTableName() {
            return Updater.DEFAULT_TABLE_NAME;
        }
    }
}

