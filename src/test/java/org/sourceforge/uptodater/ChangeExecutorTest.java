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

}
