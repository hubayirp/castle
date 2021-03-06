/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.confluent.castle.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class CastleLog implements AutoCloseable, Logger {
    private static final Logger log = LoggerFactory.getLogger(CastleLog.class);

    private final String name;
    private OutputStream outputStream;
    private final boolean enableDebug;

    public static CastleLog fromFile(String logBase, String nodeName, boolean enableDebug) throws IOException {
        File file = new File(new File(logBase), nodeName + ".clog");
        FileOutputStream outputStream = new FileOutputStream(file, true);
        CastleLog log = null;
        try {
            log = new CastleLog(nodeName, outputStream, enableDebug);
        } finally {
            if (log == null) {
                try {
                    outputStream.close();
                } catch (Throwable throwable) {
                    // Nowhere to log to
                }
            }
        }
        return log;
    }

    public static CastleLog fromStdout(String name, boolean enableDebug) throws IOException {
        return new CastleLog(name, System.out, enableDebug);
    }

    public static CastleLog fromDevNull(String name, boolean enableDebug) throws IOException {
        return new CastleLog(name, new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                // do nothing
            }
        }, enableDebug);
    }

    public CastleLog(String name, OutputStream outputStream, boolean enableDebug) {
        this.name = name;
        this.outputStream = outputStream;
        this.enableDebug = enableDebug;
    }

    public static void debugToAll(String str, CastleLog... logs) {
        for (CastleLog log : logs) {
            log.debug(str);
        }
    }

    public static void printToAll(String str, CastleLog... logs) {
        for (CastleLog log : logs) {
            log.print(str);
        }
    }

    public void print(String str) {
        try {
            String prefix = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").
                format(new Date()) + " ";
            write((prefix + str).getBytes(StandardCharsets.UTF_8));
            if (log.isTraceEnabled()) {
                if ((str.length() > 0) && (str.charAt(str.length() - 1) == '\n')) {
                    str = str.substring(0, str.length() - 1);
                }
                log.trace("{}: {}", name, str);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void printf(String format, Object... args) {
        print(String.format(format, args));
    }

    void log(String msg, Object[] objs) {
        FormattingTuple tuple = MessageFormatter.arrayFormat(msg, objs);
        if (tuple.getThrowable() == null) {
            print(tuple.getMessage() + System.lineSeparator());
        } else {
            print(tuple.getMessage() + ": " + CastleUtil.fullStackTrace(tuple.getThrowable()));
        }
    }

    void log(String msg) {
        print(msg + System.lineSeparator());
    }

    public synchronized void write(byte[] buf) throws IOException {
        if (outputStream != null) {
            outputStream.write(buf);
        }
    }

    public synchronized void write(byte[] buf, int off, int len) throws IOException {
        if (outputStream != null) {
            outputStream.write(buf, off, len);
        }
    }

    @Override
    public synchronized void close() throws IOException {
        if (outputStream != null) {
            outputStream.close();
        }
        outputStream = null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public void trace(String msg) {
        log(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        log(format, new Object[]{arg});
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        log(format, new Object[]{arg1, arg2});
    }

    @Override
    public void trace(String format, Object... arguments) {
        if (enableDebug) {
            log(format, arguments);
        }
    }

    @Override
    public void trace(String msg, Throwable t) {
        print(msg + ": " + CastleUtil.fullStackTrace(t));
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return true;
    }

    @Override
    public void trace(Marker marker, String msg) {
        trace(msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        trace(format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        trace(format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        trace(format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        trace(msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public void debug(String msg) {
        if (enableDebug) {
            log(msg);
        }
    }

    @Override
    public void debug(String format, Object arg) {
        if (enableDebug) {
            log(format, new Object[]{arg});
        }
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (enableDebug) {
            log(format, new Object[]{arg1, arg2});
        }
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (enableDebug) {
            log(format, arguments);
        }
    }

    @Override
    public void debug(String msg, Throwable t) {
        print(msg + ": " + CastleUtil.fullStackTrace(t));
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return true;
    }

    @Override
    public void debug(Marker marker, String msg) {
        debug(msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        debug(format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        debug(format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... argArray) {
        debug(format, argArray);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        debug(msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(String msg) {
        log(msg);
    }

    @Override
    public void info(String format, Object arg) {
        log(format, new Object[]{arg});
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        log(format, new Object[]{arg1, arg2});
    }

    @Override
    public void info(String format, Object... arguments) {
        log(format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        print(msg + ": " + CastleUtil.fullStackTrace(t));
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return true;
    }

    @Override
    public void info(Marker marker, String msg) {
        info(msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        info(format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        info(format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... argArray) {
        info(format, argArray);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        info(msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void warn(String msg) {
        log(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        log(format, new Object[]{arg});
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        log(format, new Object[]{arg1, arg2});
    }

    @Override
    public void warn(String format, Object... arguments) {
        log(format, arguments);
    }

    @Override
    public void warn(String msg, Throwable t) {
        print(msg + ": " + CastleUtil.fullStackTrace(t));
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return true;
    }

    @Override
    public void warn(Marker marker, String msg) {
        warn(msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        warn(format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        warn(format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... argArray) {
        warn(format, argArray);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        warn(msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public void error(String msg) {
        log(msg);
    }

    @Override
    public void error(String format, Object arg) {
        log(format, new Object[]{arg});
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        log(format, new Object[]{arg1, arg2});
    }

    @Override
    public void error(String format, Object... arguments) {
        log(format, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        print(msg + ": " + CastleUtil.fullStackTrace(t));
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return true;
    }

    @Override
    public void error(Marker marker, String msg) {
        error(msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        error(format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        error(format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... argArray) {
        error(format, argArray);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        error(msg, t);
    }
};
