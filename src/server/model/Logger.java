package server.model;
import javafx.scene.control.Label;

public class Logger {
    private Label managableArea;

    private org.apache.log4j.Logger logger;
    private LogAppender logAppender;

    public Logger(Label managableArea) {
        this.managableArea = managableArea;

        logger = org.apache.log4j.Logger.getLogger("Сервер: ");
        logAppender = new LogAppender();
        logger.addAppender(logAppender);
    }

    public void addLog(String log) {
        logger.info(log);
        managableArea.setText(logAppender.getLogs());
    }

    public String getLogs() { return logAppender.getLogs(); }
}
