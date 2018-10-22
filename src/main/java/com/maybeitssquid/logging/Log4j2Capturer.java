package com.maybeitssquid.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

public class Log4j2Capturer extends LogCapturer {

    private Logger logger = LogManager.getLogger(Log4j2Capturer.class);

    /**
     * @see LogManager#getLogger(String)
     */
    @Override
    public void setLogName(String name) {
        this.logger = LogManager.getLogger(name);
    }

    /**
     * @see ThreadContext#put(String, String)
     */
    @Override
    protected void capture(String key, String value) {
        ThreadContext.put(key, value);
    }

    /**
     * @see ThreadContext#clearMap()
     */
    @Override
    public void reset() {
        ThreadContext.clearMap();
    }

    /**
     * @see Logger#info(Object)
     */
    @Override
    protected void info(String message) {
        this.logger.info(message);
    }

    /**
     * @see Logger#warn(String)
     */
    @Override
    protected void warn(String message) {
        this.logger.warn(message);
    }

    /**
     * @see Logger#warn(String, Throwable)
     */
    @Override
    protected void warn(String message, Throwable e) {
        this.logger.warn(message, e);
    }

}
