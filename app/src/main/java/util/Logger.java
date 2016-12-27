package util;

import android.util.Log;

public class Logger {

    private final String name;

    protected Logger(String name) {
        this.name = name;
    }

    public void info(String text) {
        Log.i(name, text);
    }

    public void debug(String text) {
        Log.d(name, text);
    }

    public void trace(String text) {
        Log.v(name, text);
    }

    public void warning(String text) {
        Log.w(name, text);
    }

    public void error(String text) {
        Log.e(name, text);
    }

    public void critical(String text) {
        Log.wtf(name, text);
    }

}
