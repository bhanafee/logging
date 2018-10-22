package com.maybeitssquid.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Captures into {@link org.slf4j.MDC}
 */
public class Slf4jCapturer extends LogCapturer {

    private Logger logger = LoggerFactory.getLogger(Slf4jCapturer.class);

    /**
     * @see LoggerFactory#getLogger(String)
     */
    @Override
    public void setLogName(String name) {
        this.logger = LoggerFactory.getLogger(name);
    }

    /**
     * @see MDC#put(String, String)
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
     * @see Logger#info(String)
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
