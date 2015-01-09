Web4Log
=======

The Web4Log is a java-based web application project, using Apache Wicket as its frontend framework,
to simulate the `tail` command for other applications' log output using `SocketAppender` Log4J logger.
It helps you to monitor log4j output from one or more applications in a single web-based interface.
You need to deploy its war file in a JEE compliant server.
By default, it starts a `ServerSocket` on port 9999. Through the `config.properties` file which is located in `WEB-INF/classes`,
you can change the configuration as follows:

	`network.binding.address` : the ServerSocket binding address (default localhost)
	`network.binding.port` : the ServerSocket listening port (default 9999)
	`log.cache.max.lines` : the max number of log entry cached in the memory (default is 1000)
	`log.view.max.lines` : the max number of lines shown in the web-based interface (default is 1500)
	`log.layout` : the log4j layout template (default is [%p] (%-9t %d) %m%n)

To enable a client application to connect to Web4Log, you should modify your `log4j.xml` file as follows:

First, add an appender like the following one:

```xml
<appender name="REMOTE" class="org.apache.log4j.net.SocketAppender">
	<param name="remoteHost" value="localhost"/>
	<param name="port" value="9999"/>
	<param name="application" value="test"/>
</appender>
```

The "remoteHost" and "port" are the connection parameters. The "application" parameter is mandatory, and helps you
select the associated application log output in Web4Log interface.

Second, use the appender name (in this case "REMOTE") in any of your logger item(s), like below:

```xml
<logger name="..." additivity="false">
	<level value="INFO"/>
	<appender-ref ref="REMOTE"/>
	...
</logger>

<root>
	<level value="ERROR"/>
	<appender-ref ref="REMOTE"/>
	...
</root>
```

To build the war file, you can clone the project, and execute `mvn package` in the project's root directory.