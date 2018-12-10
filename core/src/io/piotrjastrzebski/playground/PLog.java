package io.piotrjastrzebski.playground;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.StringBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PLog {
    private static SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss:SSS");
    private static final boolean clickable = true;
    private static final boolean threadInfo = true;

    public static void log(String message) {
        if (Gdx.app != null) {
            Gdx.app.log(now(), message);
        }
    }

    public static void error(String message) {
        Gdx.app.error(now(), message);
    }

    public static void error(String message, Throwable e) {
        if (e != null) {
            Gdx.app.error(now(), message, e);
        } else {
            Gdx.app.error(now(), message);
        }
    }

    public static void debug(String message) {
        Gdx.app.debug(now(), message);
    }

    private static StringBuilder sb = new StringBuilder();
    private static String now() {
        sb.setLength(0);
        sb.append(sdf.format(new Date()));
        if (clickable) {
            // we use 3 here, as we want to go up until we leave logging stuff to the caller
            // 0 is this method, 1 is one of methods in this class, 2 is the PMLog wrapper
            // magic format so idea picks it up
            clickableIdeaLog(3, sb, false, false);
        }
        if (threadInfo) {
            Thread ct = Thread.currentThread();
            sb
                .append("[")
                .append(ct.getName()).append(",")
                .append(ct.getPriority()).append(",")
                .append(ct.getThreadGroup().getName())
                .append("]");
        }
        return sb.toString();
    }

    private static StringBuilder clickableIdeaLog(int level, StringBuilder sb, boolean methodName, boolean innerClasses) {
        StackTraceElement[] stackTrace = new Exception().getStackTrace();
        StackTraceElement ste = stackTrace[level];
        sb.append(".(").append(ste.getFileName()).append(":").append(ste.getLineNumber()).append(")");
        if (innerClasses) {
            String className = ste.getClassName();
            int indexOf = className.indexOf("$");
            if (indexOf != -1) {
                sb.append(className.substring(indexOf));
            }
        }
        if (methodName) {
            sb.append("#").append(ste.getMethodName());
        }
        return sb;
    }
}
