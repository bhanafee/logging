package com.maybeitssquid.logging;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

public class Log4jCapturer extends LogCapturer {

    private Logger logger = Logger.getLogger(Log4jCapturer.class);

    /**
     * @see Logger#getLogger(String)
     */
    @Override
    public void setLogName(String name) {
        this.logger = Logger.getLogger(name);
    }

    /**
     * @see MDC#put(String, Object)
     */
    @Override
    protected void capture(String key, String value) {
        MDC.put(key, value);
    }

    /**
     * @see MDC#clear()
     */
    @Override
    public void reset() {
        MDC.clear();
    }

    /**
     * @see Logger#info(Object)
     */
    @Override
    protected void info(String message) {
        this.logger.info(message);
    }

    /**
     * @see Logger#warn(Object)
     */
    @Override
    protected void warn(String message) {
        this.logger.warn(message);
    }

    /**
     * @see Logger#warn(Object, Throwable)
     */
    @Override
    protected void warn(String message, Throwable e) {
        this.logger.warn(message, e);
    }
}
