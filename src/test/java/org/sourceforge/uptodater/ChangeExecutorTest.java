package org.sourceforge.uptodater;

import junit.framework.TestCase;

public class ChangeExecutorTest extends TestCase {

    public void testChangeExecutor() throws Exception {

        String statementChangeExecutorText = "should be statement change executor";
        ChangeExecutor changeExecutor = ChangeExecutor.createChangeExecutor(statementChangeExecutorText);
        assertNotNull(changeExecutor.sqlText);
        assertNotNull(changeExecutor);
        assertTrue(changeExecutor instanceof ChangeExecutor.StatementChangeExecutor);
        String callableChangeExecutorText = "cAll should be callable change executor";
        changeExecutor = ChangeExecutor.createChangeExecutor(callableChangeExecutorText);
        assertNotNull(changeExecutor);
        assertTrue(changeExecutor instanceof ChangeExecutor.CallableChangeExecutor);

    }

    public void testOptionalHintOld() {
        String optionalThing = "-- optional\n" +
                " this ain't even sql";
        ChangeExecutor ce = ChangeExecutor.createChangeExecutor(optionalThing);
        assertTrue(ce.isOptional());
        String notOptionalThing = "\n" +
                " this ain't even sql";
        ce = ChangeExecutor.createChangeExecutor(notOptionalThing);
        assertFalse(ce.isOptional());
    }

    public void testOptionalHint() {
        String optionalThing = "-- statement.optional\n" +
                " this ain't even sql";
        ChangeExecutor ce = ChangeExecutor.createChangeExecutor(optionalThing);
        assertTrue(ce.isOptional());
        String notOptionalThing = "\n" +
                " this ain't even sql";
        ce = ChangeExecutor.createChangeExecutor(notOptionalThing);
        assertFalse(ce.isOptional());
    }

}
