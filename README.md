1.) Edit the snowflake.properties file with the necessary configurations for your
    environment (for example, authenticator, username, etc...)

2.) Edit the snowflake.sql file with the SQL statements you would like
    to execute on Snowflake. It supports multi-line statements, however,
    you must end your query with a ; character. Each query needs to start
    on a new line.

3.) You can use the run_linux.sh and run_windows.bat scripts to run the 
    application on a Linux or a Windows environment, respectively. 

4.) If you need to add connection parameters, then use the connection.properties
    file. This is ignored unless you pass an argument to the application. You can
    edit the run_* scripts and add the value 'true' at the very end, for example:

    SnowflakeJdbcDebugApp true

5.) After the application runs, it will generate a log file called 
    snowflake_jdbc.log in the same directory where the scripts reside.

6.) To change the Snowflake JDBC driver version edit the run_* scripts.

7.) Add any JVM arguments to the run_* scripts (for example: -Xmx2g).
