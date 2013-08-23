package org.sourceforge.uptodater;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SimpleUpToDateRunner extends UpToDateRunner {
    private final String jdbc;
    private final String user;
    private final String pass;
    private final String table;
    private final String zip;

    public SimpleUpToDateRunner(String jdbc, String user, String pass, String table, String zip) {
        this.jdbc = jdbc;
        this.user = user;
        this.pass = pass;
        this.table = table;
        this.zip = zip;
    }

    @Override
    protected Connection getConnection() {
        try {
            System.out.printf("Connecting with %s\n  user: '%s'\n", jdbc, user);
            return DriverManager.getConnection(jdbc, user, pass);
        } catch (SQLException e) {
            throw new ConfigurationException(
                    String.format("Unable to connect to database with %s/%s", jdbc, user), e);
        }
    }

    @Override
    public String getTableName() {
        return table;
    }


    @Override
    protected String getDatasourceName() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected String getUpDateZip() {
        return zip;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        OptionParser parser = new OptionParser();
        parser.accepts("driver").withRequiredArg().ofType(String.class).describedAs("JDBC Driver Class");
        parser.accepts("jdbc").withRequiredArg().ofType(String.class).describedAs("JDBC Connect String");
        parser.accepts("user").withRequiredArg().ofType(String.class).describedAs("Database Username");
        parser.accepts("pass").withRequiredArg().ofType(String.class).describedAs("Database User Password");
        parser.accepts("table").withRequiredArg().ofType(String.class).describedAs("Table to store updates in");
        parser.accepts("zip").withRequiredArg().ofType(String.class).describedAs("ZIP file to load updates from");
        parser.accepts("dry").withOptionalArg().ofType(Boolean.class).describedAs("Don't actually execute any new updates");

        OptionSet optionSet = parser.parse(args);

        boolean allOptionsPresent = true;
        allOptionsPresent = validate(allOptionsPresent, optionSet, "jdbc");
        allOptionsPresent = validate(allOptionsPresent, optionSet, "user");
        allOptionsPresent = validate(allOptionsPresent, optionSet, "pass");
        allOptionsPresent = validate(allOptionsPresent, optionSet, "table");
        allOptionsPresent = validate(allOptionsPresent, optionSet, "zip");

        if (!allOptionsPresent) {
            System.err.println("One or more required options was missing.");
            parser.printHelpOn(System.err);
            System.exit(1);
        }

        Class.forName(String.valueOf(optionSet.valueOf("driver")));

        String jdbc = (String) optionSet.valueOf("jdbc");
        String user = (String) optionSet.valueOf("user");
        String pass = (String) optionSet.valueOf("pass");
        String table = (String) optionSet.valueOf("table");
        String zip = (String) optionSet.valueOf("zip");
        SimpleUpToDateRunner runner = new SimpleUpToDateRunner(jdbc, user, pass, table, zip);
        runner.doUpdate(optionSet.has("dry"));
    }

    private static boolean validate(boolean allOptionsPresent, OptionSet optionSet, String option) {
        return allOptionsPresent && optionSet.has(option);
    }
}
