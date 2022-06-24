java -cp .;debug-app.jar;lib\snowflake-jdbc-3.13.7.jar;lib\bcpkix-fips-1.0.5.jar;lib\bc-fips-1.0.2.1.jar;.\log4j\slf4j-api-1.7.30.jar;.\log4j\slf4j-log4j12-1.7.30.jar;.\log4j\log4j-1.2.17.jar ^
-Dlog4j.configuration=file:\\\%cd%\log4j\log4j.properties ^
-Dnet.snowflake.jdbc.loggerImpl=net.snowflake.client.log.SLF4JLogger ^
SnowflakeJdbcDebugApp
