package com.maybeitssquid.logging;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class NOOPCapturer extends LogCapturer {
    @Override
    public void webLog(ServletRequest request, ServletResponse response, Throwable e) {
        // EMPTY
    }

    @Override
    protected void capture(String key, String value) {
        // EMPTY
    }

    @Override
    public void reset() {
        // EMPTY
    }

    @Override
    public void setLogName(String name) {
        // EMPTY
    }

    @Override
    protected void info(String message) {
        // EMPTY
    }

    @Override
    protected void warn(String message) {
        // EMPTY
    }

    @Override
    protected void warn(String message, Throwable e) {
        // EMPTY
    }
}
