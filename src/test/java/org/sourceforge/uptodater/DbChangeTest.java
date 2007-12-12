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

    public void testAlternateSeparator() {
        String twoProcedures = "-- uptodater.statement.separator=^/\n" +
                "\n" +
                "\n" +
                "CREATE OR REPLACE PROCEDURE GET_LAST_CONTACT (\n" +
                "  p_party_id in  number,\n" +
                "  p_own_id in  number,\n" +
                "  p_evt_id out number,\n" +
                "  p_evt_date out date,\n" +
                "  p_evt_type out varchar2\n" +
                "  )\n" +
                "IS\n" +
                "BEGIN\n" +
                "  BEGIN\n" +
                "    SELECT lc.CONTACT_DATE, lc.CONTACT_ID, decode(lc.CONTACT_TYPE,'M','MeetingBean','E','Email','Unknown') INTO p_evt_date, p_evt_id, p_evt_type\n" +
                "      FROM LAST_CONTACTS lc\n" +
                "      WHERE lc.OWN_ID = p_own_id AND lc.PARTY_ID = p_party_id AND NEEDS_UPDATE = 0\n" +
                "      ORDER BY lc.CONTACT_DATE DESC;                       \n" +
                "  EXCEPTION WHEN NO_DATA_FOUND THEN\n" +
                "    NULL;\n" +
                "  END;\n" +
                "\n" +
                "END;\n" +
                "\n" +
                "/\n" +
                "\n" +
                "CREATE OR REPLACE PROCEDURE GET_LAST_CONTACT_CLASSIC (\n" +
                "  p_party_id in  number,\n" +
                "  p_own_id in  number,\n" +
                "  p_evt_id out number,\n" +
                "  p_evt_date out date,\n" +
                "  p_evt_type out varchar2\n" +
                "  )\n" +
                "IS\n" +
                "  p_evt_date2 date;\n" +
                "  p_evt_id2 number;\n" +
                "  p_evt_type2 varchar2(256);\n" +
                "BEGIN\n" +
                "\n" +
                "  BEGIN\n" +
                "  select MSG_DATE, 'MeetingBean', MSG_SUMMARY_ID\n" +
                "         into p_evt_date, p_evt_type, p_evt_id\n" +
                "    from (select s.start_date MSG_DATE, S.MEETING_ID MSG_SUMMARY_ID\n" +
                "            from MEETINGS S\n" +
                "           where s.own_id = p_own_id AND\n" +
                "                 s.start_date < SYSDATE\n" +
                "                and  (       exists (select * from people_meetings_mapping pmm where pmm.meeting_id = s.MEETING_ID and pmm.PARTY_ID = p_party_id)\n" +
                "                         or  exists (select * from links l where l.meeting_id = s.MEETING_ID  and l.party_id = p_party_id)\n" +
                "                )\n" +
                "        order by s.start_date desc\n" +
                "     ) where rownum  = 1;\n" +
                " EXCEPTION\n" +
                "  WHEN NO_DATA_FOUND THEN\n" +
                "    dbms_output.put_line('No meetings found');\n" +
                "  END;\n" +
                "\n" +
                "  BEGIN\n" +
                "     select  MSG_DATE, 'Email', MSG_SUMMARY_ID\n" +
                "             into  p_evt_date2, p_evt_type2, p_evt_id2\n" +
                "       from  (select s.MSG_DATE as MSG_DATE, m.MSG_SUMMARY_ID\n" +
                "                   from parties_cms_mappings m,\n" +
                "                        contact_message_summaries s\n" +
                "               where\n" +
                "               m.MSG_SUMMARY_ID = s.MSG_SUMMARY_ID\n" +
                "               and s.IS_DELETED = 0\n" +
                "               and m.PARTY_ID = p_party_id\n" +
                "               order by MSG_DATE desc\n" +
                "               ) where rownum = 1;\n" +
                "    if (p_evt_date2 > nvl(p_evt_date,sysdate-4000)) then\n" +
                "      p_evt_date := p_evt_date2;\n" +
                "      p_evt_id := p_evt_id2;\n" +
                "      p_evt_type := p_evt_type2;\n" +
                "    end if;\n" +
                "\n" +
                "    EXCEPTION\n" +
                "      WHEN NO_DATA_FOUND THEN\n" +
                "        dbms_output.put_line('No email found');\n" +
                "    END;\n" +
                "\n" +
                "END;\n" +
                "/";
        DbChange dbChange = new DbChange(id, twoProcedures, date, description, null);
        assertEquals(2, dbChange.getSqlChanges().size());


    }
}
