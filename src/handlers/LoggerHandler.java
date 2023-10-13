package handlers;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerHandler {
    public static void loadLoggerSettings(Logger logger){
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
            SimpleFormatter formatter = new SimpleFormatter(){
                @Override
                public String format(LogRecord record) {
                    StringBuilder sb = new StringBuilder();
                    sb.append('[').append(record.getLoggerName()).append("] - ");
                    sb.append(dateFormat.format(new Date(record.getMillis()))).append(" - ");
                    sb.append(record.getLevel().getLocalizedName()).append(" - ");
                    sb.append(record.getMessage()).append('\n');
                    if(record.getThrown() != null)
                        sb.append(record.getThrown()).append('\n');
                    return sb.toString();
                }
            };
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
            FileHandler fileHandler = new FileHandler(dateFormatter.format(new Date()) + ".log");
            fileHandler.setFormatter(formatter);
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(formatter);
            logger.addHandler(fileHandler);
            logger.addHandler(consoleHandler);
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            logger.warning("Failed to open log file. Logs won't be saved.");
        }
    }
}
