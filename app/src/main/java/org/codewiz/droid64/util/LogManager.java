package org.codewiz.droid64.util;

/**
 * Created by roland on 25.08.2016.
 */

public class LogManager {

    public static Logger getLogger(String name) {
        Logger logger = new Logger(name);
        return logger;
    }
}
