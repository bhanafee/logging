package com.maybeitssquid.logging;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.UUID;

/**
 * Capture loggable values from a servlet request and response.
 */
public abstract class LogCapturer {

    /**
     * Key to the application ID
     */
    public static final String APPLICATION_ID = "applicationId";

    /**
     * Key to the log event type for web log events.
     */
    public static final String LOG_EVENT_TYPE = "logEventType";

    /**
     * Key to the request ID
     */
    public static final String REQUEST_ID = "WFRequestID";

    /**
     * Formatter for timestamps that follows RFC-3339 with forced millisecond precision and offset time zone style.
     */
    public static final DateTimeFormatter TIMESTAMP;

    static {
        TIMESTAMP = new DateTimeFormatterBuilder()
                .append(DateTimeFormatter.ISO_LOCAL_DATE)
                .appendLiteral('T')
                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .appendLiteral(':')
                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                .appendFraction(ChronoField.MILLI_OF_SECOND, 3, 3, true)
                .appendOffset("+HH:MM", "+00:00")
                .toFormatter(Locale.US);
    }

    private String applicationId;

    private String webLogEventType;

    /**
     * Whether to captureIfPresent raw strings when parsing fails.
     */
    private boolean fallbackToString = false;

    /**
     * Time zone to use for formatted timestamps
     */
    private ZoneId timeZone = ZoneId.systemDefault();

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * Whether to fall back to capturing a raw header value if an int, date or UUID parse fails.
     *
     * @return whether to captureIfPresent raw strings
     */
    public boolean isFallbackToString() {
        return this.fallbackToString;
    }

    /**
     * Whether to fall back to capturing a raw header value if an int, date or UUID parse fails.
     *
     * @param fallbackToString whether to captureIfPresent raw strings
     */
    public void setFallbackToString(final boolean fallbackToString) {
        this.fallbackToString = fallbackToString;
    }

    /**
     * Gets the time zone used to format dates. Default is {@link ZoneId#systemDefault()}
     *
     * @return the time zone used to format dates.
     */
    public ZoneId getTimeZone() {
        return this.timeZone;
    }

    /**
     * Sets the time zone used to format dates. Default is {@link ZoneId#systemDefault()}
     *
     * @param timeZone the time zone used to format dates.
     */
    public void setTimeZone(final ZoneId timeZone) {
        if (timeZone != null) {
            this.timeZone = timeZone;
        }
    }

    public void setWebLogEventType(String webLogEventType) {
        this.webLogEventType = webLogEventType;
    }

    public boolean isWebLogging() {
        return this.webLogEventType != null;
    }

    public String getWebLogEventType() {
        return this.webLogEventType == null ? "web" : this.webLogEventType;
    }

    public void webLog(ServletRequest request, ServletResponse response) {
        webLog(request, response, null);
    }


    /**
     * Creates a log of the servlet request and response.
     *
     * @param request  the servlet request
     * @param response the servlet response
     * @param e        exception that was thrown while processing this request, or {@code null}
     */
    public void webLog(final ServletRequest request, final ServletResponse response, final Throwable e) {
        captureWebLog(request, response);
        capture(LOG_EVENT_TYPE, getWebLogEventType());

        if (e != null) {
            warn("", e);
        } else if (response instanceof HttpServletResponse && ((HttpServletResponse) response).getStatus() >= 400) {
            warn("");
        } else {
            info("");
        }
    }

    abstract protected void info(final String message);
    abstract protected void warn(final String message);
    abstract protected void warn(final String message, final Throwable e);

    /**
     * Method to record a key-value pair that has been captured from the request or response.
     *
     * @param key   the name of the captured field
     * @param value the value to record
     */
    abstract protected void capture(final String key, final String value);

    /**
     * Method to reset the logging context.
     */
    abstract public void reset();

    /**
     * Method to set the name of the logger.
     * @param name the name of the logger.
     */
    abstract public void setLogName(final String name);

    public void captureWebLog(final ServletRequest request, final ServletResponse response) {
        captureServletRequest(request);
        if (request instanceof HttpServletRequest) {
            captureHttpRequest((HttpServletRequest) request);
            captureHeaders((HttpServletRequest) request);
            // Safe conversion b/c HttpServletRequest always pairs with HttpServletResponse
            captureHttpResponse((HttpServletResponse) response);
        }
    }

    /**
     * Records non-null, non-empty values.
     *
     * @param key   the name of the captured field
     * @param value the value to record
     * @see #capture(String, String)
     */
    private void captureIfPresent(final String key, final String value) {
        if (value != null && !value.isEmpty()) {
            capture(key, value);
        }
    }

    /**
     * Records the application id and request id, generating a new request id if necessary.
     *
     * @param request the {@link ServletRequest}
     */
    public void captureLogContext(final ServletRequest request) {
        captureIfPresent(APPLICATION_ID, getApplicationId());
        String id = request instanceof HttpServletRequest ? fromUuid((HttpServletRequest) request, "WF-Request-ID") : null;
        if (id == null || id.isEmpty()) {
            capture(REQUEST_ID, UUID.randomUUID().toString());
        } else {
            capture(REQUEST_ID, id);
        }
    }

    /**
     * Capture network parameters that apply to any request.
     *
     * @param request the {@link ServletRequest}
     */
    public void captureServletRequest(final ServletRequest request) {
        capture("src_ip", request.getRemoteAddr());
        capture("src_port", Integer.toString(request.getRemotePort()));
        capture("dest_ip", request.getLocalAddr());
        capture("dest_port", Integer.toString(request.getLocalPort()));
        final int bytes_in = request.getContentLength();
        if (bytes_in > -1) {
            capture("bytes_in", Integer.toString(bytes_in));
        }
    }

    /**
     * Capture of the HTTP response.
     *
     * @param response the {@link HttpServletResponse}
     */
    public void captureHttpResponse(final HttpServletResponse response) {
        capture("http_status", Integer.toString(response.getStatus()));
    }

    /**
     * Capture properties of the HTTP request itself.
     *
     * @param request the {@link HttpServletRequest}
     */
    public void captureHttpRequest(final HttpServletRequest request) {
        capture("http_method", request.getMethod());
        final String servlet = request.getServletPath();
        final String path = request.getPathInfo();
        captureIfPresent("uri_path", (servlet == null ? "" : servlet) + (path == null ? "" : path));
        captureIfPresent("uri_query", request.getQueryString());
        capture("url", request.getRequestURL().toString());
        capture("protocol", request.getProtocol());
    }

    public void captureHeaders(final HttpServletRequest request) {
        captureIfPresent("date", fromDate(request, "Date"));
        captureIfPresent("http_host", request.getHeader("Host"));
        captureIfPresent("http_referrer", request.getHeader("Referer"));
        captureIfPresent("http_user_agent", request.getHeader("User-Agent"));
        captureIfPresent("http_content_type", request.getHeader("Content-Type"));
        captureIfPresent("x_forwarded_for", request.getHeader("X-Forwarded-For"));
        captureIfPresent("keep_alive", request.getHeader("Keep-Alive"));
    }

    protected String fromDate(final HttpServletRequest req, final String header) {
        ZonedDateTime ts = null;
        try {
            // Easy way first
            ts = Instant.ofEpochMilli(req.getDateHeader(header)).atZone(getTimeZone());
        } catch (final IllegalArgumentException e) {
            // EMPTY
        }
        if (ts == null) {
            try {
                // More permissive parser
                ts = ZonedDateTime.parse(req.getHeader(header), DateTimeFormatter.ISO_ZONED_DATE_TIME);
            } catch (final DateTimeException | ArithmeticException e) {
                // EMPTY
            }
        }

        if (ts != null) {
            return TIMESTAMP.format(ts);
        } else {
            return isFallbackToString() ? req.getHeader(header) : null;
        }
    }

    protected String fromUuid(final HttpServletRequest req, final String header) {
        final String raw = req.getHeader(header);
        try {
            return UUID.fromString(raw).toString();
        } catch (final IllegalArgumentException | NullPointerException e) {
            return isFallbackToString() ? raw : null;
        }
    }

    protected String fromInt(final HttpServletRequest req, final String header) {
        try {
            return Integer.toString(req.getIntHeader(header));
        } catch (final NumberFormatException e) {
            return isFallbackToString() ? req.getHeader(header) : null;
        }
    }
}
