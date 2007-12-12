package org.sourceforge.uptodater;

import junit.framework.TestCase;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;

public class DbChangeTest extends TestCase {

    private String id;
    private Date date;
    private String description;
    protected void setUp() throws Exception {
        super.setUp();
        id = "42";
        date = new Date();
        description = "";
    }

    public void testSplitChangesOneChange() throws Exception {

        String expected = "TEST STRING";
        String sqlText = "TEST STRING";
        Date appliedDate = null;
        DbChange dbChange = new DbChange(id, sqlText, date, description, appliedDate);
        List<String>sqlChanges = dbChange.getSqlChanges();
        assertNotNull(sqlChanges);
        assertEquals(1, sqlChanges.size());
        assertEquals(expected, sqlChanges.get(0));

        sqlText = "   TEST STRING\n\n ; \n";
        dbChange = new DbChange(id, sqlText, date, description, appliedDate);
        sqlChanges = dbChange.getSqlChanges();
        assertNotNull(sqlChanges);
        assertEquals(1, sqlChanges.size());
        assertEquals(expected, sqlChanges.get(0));

    }

    public void testMultipleLines() throws Exception {
        String expected1 = "expected1";
        String expected2 = "expected2";
        String sqlText = ";;;\n;\n;    " + expected1 + "  \t\t\t\t\t\n;;;;;       " + expected2 + "   ;;;;\n";

        DbChange dbChange = new DbChange(id, sqlText, date, description,null);
        List<String>sqlChanges = dbChange.getSqlChanges();
        assertNotNull(sqlChanges);
        assertEquals(expected1, sqlChanges.get(0));
        assertEquals(expected2,  sqlChanges.get(1));
        assertEquals(2, sqlChanges.size());
    }

    public void testIsOptionalChange() throws Exception {
        DbChange dbChange = new DbChange(id, null, date, description, null);
        assertFalse(dbChange.isOptional());
        dbChange = new DbChange(id, "", date, description, null);
        assertFalse(dbChange.isOptional());
        String changeText = generateRandomChangeText(5);
        changeText = changeText + " \n-- uptodater.optional=false";
        dbChange = new DbChange(id, changeText, date, description, null);
        assertFalse(dbChange.isOptional());
        changeText = generateRandomChangeText(5);
        changeText = changeText + " \n-- uptodater.optional=asdlkfjas;lf";
        dbChange = new DbChange(id, changeText, date, description, null);
        assertFalse(dbChange.isOptional());

        changeText = generateRandomChangeText(5);
        changeText = changeText + " \n-- uptodater.optional   =    true";
        dbChange = new DbChange(id, changeText, date, description, null);
        assertTrue(dbChange.isOptional());

        changeText = "--uptodater.optional=true";
        dbChange = new DbChange(id, changeText, date, description, null);
        assertTrue(dbChange.isOptional());
    }

    private String generateRandomChangeText(int numberOfLines) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < numberOfLines; i ++) {
            builder.append(RandomStringUtils.randomAlphabetic(24)).append("\n");
        }
        return builder.toString();
    }
}
