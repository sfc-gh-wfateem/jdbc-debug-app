java -Dlog4j.configuration=.\log4j\log4j.properties -Dnet.snowflake.jdbc.loggerImpl=net.snowflake.client.log.SLF4JLogger -cp .;snowflake-jdbc-3.13.3.jar;.\log4j\log4j-1.2.17.jar;.\log4j\slf4j-api-1.7.30.jar;.\log4j\slf4j-log4j12-1.7.30.jar SnowflakeJDBCExample
