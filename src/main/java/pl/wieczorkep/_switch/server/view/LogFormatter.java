package pl.wieczorkep._switch.server.view;

import java.time.Instant;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {
    @Override
    public String format(LogRecord logRecord) {
        return '[' + Instant.ofEpochMilli(logRecord.getMillis()).toString() + ']'
                + '[' + logRecord.getLevel() + ']'
                + " ThreadID:" + logRecord.getThreadID() + " "
                + logRecord.getSourceClassName() + "::" + logRecord.getSourceMethodName()
                + " | " + logRecord.getMessage() + "\n";
    }
}
