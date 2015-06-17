# Fool.com Servlet Filters

This is a small java project that provides the following two servlet filters:

## PoweredByResponseHeaderFilter

This filter inserts a header into the response with the server name and network
port that serviced the request.

```xml
<filter>
  <filter-name>PoweredByResponseHeaderFilter</filter-name>
  <filter-class>com.fool.servlet.PoweredByResponseHeaderFilter</filter-class>
  <init-param>
  	<param-name>header</param-name>
  	<param-value>X-Powered-By</param-value>
</filter>

<filter-mapping>
  <filter-name>PoweredBy</filter-name>
  <url-pattern>/*</url-pattern>
</filter-mapping>
```

The `header` init-param is optional and the default value is `X-Powered-By`.

Once configured, the server will respond like:

    curl -s -i http://localhost:8983/solr/ | head
    HTTP/1.1 200 OK
    X-Powered-By: localhost:8983

## RequestHeaderMDCFilter

This filter binds headers from an http servlet request into log4j's
Mapped Diagnostic Context (MDC) so that header values can be logged
in statements made while servicing the request.

Multiple headers can be bound by delimiting them with commas.

```xml
<filter>
  <filter-name>RequestHeaderMDCFilter</filter-name>
  <filter-class>com.fool.servlet.RequestHeaderMDCFilter</filter-class>
  <init-param>
    <param-name>headers</param-name>
    <param-value>X-Request-ID, User-Agent</param-value>
  </init-param>
</filter>
   
<filter-mapping>
  <filter-name>RequestHeaderMDCFilter</filter-name>
  <url-pattern>/*</url-pattern>
</filter-mapping>
```

Then, in log4j.properties (or elsewhere):

    log4j.appender.file.layout.ConversionPattern=%-5p - %d{yyyy-MM-dd HH:mm:ss.SSS}; %C; RequestId: %X{X-Request-ID} %m\n

The mdc property name can be something other than the header name by using `header:mdc_key` pairs:

```xml
<filter>
  <filter-name>RequestHeaderMDCFilter</filter-name>
  <filter-class>com.fool.servlet.RequestHeaderMDCFilter</filter-class>
  <init-param>
    <param-name>headers</param-name>
    <param-value>X-Request-ID:request_id</param-value>
  </init-param>
</filter>
```

Then, in log4j.properties (or elsewhere):

    log4j.appender.file.layout.ConversionPattern=%-5p - %d{yyyy-MM-dd HH:mm:ss.SSS}; %C; request_id:%X{request_id} %m\n

## MessageParsingMDCLayout

This library also includes a delegating log4j layout that uses regex to
parse a formatted message into MDC properties for structured logging.

In practice this layout was developed to give structure to Solr logs so that
metadata like hits, query time and status can be sent as separate fields in
a JSON-formatted output suitable for integration with LogStash.

Example configuration:

```
#- size rotation with log cleanup.
log4j.appender.file=org.apache.log4j.RollingFileAppender
log4j.appender.file.MaxFileSize=4MB
log4j.appender.file.MaxBackupIndex=9

#- File to log to and log format
log4j.appender.file.File=${solr.log}/solr.log
log4j.appender.file.layout=com.fool.log4j.layout.MessageParsingMDCLayout
log4j.appender.file.layout.regex=^\\[([^\\]]+)\\] webapp=([\\S]+) path=([\\S]+) params=([\\S]+) (?:hits=([\\d]+) )?status=([\\d]+) QTime=([\\d]+).*$
log4j.appender.file.layout.fieldNames=core, webapp, path, params, hits, status, qtime
log4j.appender.file.layout.layout=net.logstash.log4j.JSONEventLayoutV1
```
