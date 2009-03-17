package org.sourceforge.uptodater;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class StatementPreparer {
    private Log logger = LogFactory.getLog(getClass());
    private final ConfigData configData;
    private final Pattern pattern;

    public StatementPreparer(ConfigData configData) {
        this.configData = configData;
        pattern = Pattern.compile("([^\\\\]\\{.*?[^\\\\]\\})");
    }

    public String prepare(final String statement) throws ConfigurationException{
        String toReplace = statement;
        Matcher matcher = pattern.matcher(statement);
        while(matcher.find()) {
            String match = matcher.group();
            String key = match.trim();
            key = key.substring(1, key.length() - 1);
            String replacement = configData.get(key, null);
            if(replacement == null) {
                String errorMessage = "Configuration error, expecting property " + key + ", but was not found.  Does your sql have a string with unescaped {braces}?";
                ConfigurationException exception = new ConfigurationException(errorMessage);
                logger.error(exception);
                throw exception;
            }
            toReplace = toReplace.replace(match, replacement);
        }

        toReplace = toReplace.replace("\\{", "{");
        toReplace = toReplace.replace("\\}", "}");
        return toReplace;
    }
}
