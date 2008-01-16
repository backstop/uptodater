package org.sourceforge.uptodater;

import junit.framework.TestCase;

import java.util.Date;

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

    public void testOptionalHint() {
        String optionalThing = "-- optional\n" +
                " this ain't even sql";
        ChangeExecutor ce = ChangeExecutor.createChangeExecutor(optionalThing);
        assertTrue(ce.isOptional());
        String notOptionalThing = "\n" +
                " this ain't even sql";
        ce = ChangeExecutor.createChangeExecutor(notOptionalThing);
        assertFalse(ce.isOptional());


    }

}
