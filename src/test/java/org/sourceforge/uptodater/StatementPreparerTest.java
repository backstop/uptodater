package org.sourceforge.uptodater;

import junit.framework.TestCase;

public class StatementPreparerTest extends TestCase {

    private ConfigData configData;
    protected void setUp() throws Exception {
        super.setUp();
        final String overRideKey = "param1";
        final String overRideKey2 = "param2";
        final String overRideValue = "TEST";

        configData = new ConfigData() {
            public void addOverrideConfiguration(String configurationName) {
               configuration.put(overRideKey, overRideValue);
               configuration.put(overRideKey2, overRideValue);
            }
        };
    }

    public void testNoParameters() throws Exception {
        StatementPreparer statementPreparer = new StatementPreparer(configData);

        String testStatementNoOverrides = "INSERT INTO TEST (a) value (1)";
        assertNotNull(statementPreparer.prepare(testStatementNoOverrides));
        assertEquals(testStatementNoOverrides, statementPreparer.prepare(testStatementNoOverrides));
    }
    public void testOneParameter() throws Exception {
        StatementPreparer statementPreparer = new StatementPreparer(configData);

        String testStatementOneOverride = "INSERT INTO {param1} (a,b) VALUES(1,1)";
        String expectedValue = "INSERT INTO TEST (a,b) VALUES(1,1)";
        assertNotNull(statementPreparer.prepare(testStatementOneOverride));
        assertEquals(expectedValue, statementPreparer.prepare(testStatementOneOverride));
    }

    public void testOneParameterWithSpaces() throws Exception {
        StatementPreparer statementPreparer = new StatementPreparer(configData);

        String testStatementOneOverride = "INSERT INTO { param1       } (a,b) VALUES(1,1)";
        String expectedValue = "INSERT INTO TEST (a,b) VALUES(1,1)";
        assertNotNull(statementPreparer.prepare(testStatementOneOverride));
        assertEquals(expectedValue, statementPreparer.prepare(testStatementOneOverride));

    }

    public void testTwoParameters() throws Exception {
        StatementPreparer statementPreparer = new StatementPreparer(configData);

        String testStatementOneOverride = "INSERT INTO {param1} {param2} (a,b) VALUES(1,1)";
        String expectedValue = "INSERT INTO TEST TEST (a,b) VALUES(1,1)";
        assertNotNull(statementPreparer.prepare(testStatementOneOverride));
        assertEquals(expectedValue, statementPreparer.prepare(testStatementOneOverride));
    }

    public void testParameterExpectedNotFound() throws Exception {
         StatementPreparer statementPreparer = new StatementPreparer(configData);
         String statementString = "INSERT INTO {NOTFOUND} (a,b) VALUES(1,1)";

        try {
            statementPreparer.prepare(statementString);
            fail();
        } catch (Exception e) {
            //good
        }
    }
}
