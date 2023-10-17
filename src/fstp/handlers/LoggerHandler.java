package fstp.handlers;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerHandler {
    /**
     * Load logger settings
     * 
     * @param logger Logger to load settings to
     * @param file  If true, save logs to file
     */
    public static void loadLoggerSettings(Logger logger, boolean file){
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
            
            if (file) {
                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
                FileHandler fileHandler = new FileHandler(dateFormatter.format(new Date()) + ".log");
                fileHandler.setFormatter(formatter);
                logger.addHandler(fileHandler);
            }

            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(formatter);
            logger.addHandler(consoleHandler);
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            logger.warning("Failed to open log file. Logs won't be saved.");
        }
    }
}
