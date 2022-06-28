java -cp debug-app.jar:lib/snowflake-jdbc-3.13.5.jar:./log4j/slf4j-api-1.7.30.jar:./log4j/slf4j-log4j12-1.7.30.jar:./log4j/log4j-1.2.17.jar \
-Dlog4j.debug=true \
-Dlog4j.configuration=file:///$(pwd)/log4j/log4j.properties \
-Dnet.snowflake.jdbc.loggerImpl=net.snowflake.client.log.SLF4JLogger \
SnowflakeJdbcDebugApp
