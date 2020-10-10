package pri.lr.lucene_1.demo;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public Logger(String name) {
        this.name = name;
    }

    enum Level{
        trace,
        debug,
        info,
        warn,
        error
    }

    private String name;

    static Logger get(String name) {
        return new Logger(name);
    }

    public void log(String message) {
        System.out.printf("[%s][%s] %s", sdf.format(new Date().getTime()), this.name, message);
    }
}
