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
    edit the run_* scripts and add the value 'true' at the very end. At this time,
    you have to provide exactly two boolean arguments. The second boolean argument
    is to enable Hikari Connection Pooling:

    //Read connection parameters from connection.properties file and don't use Hikari:
    SnowflakeJdbcDebugApp true false

    //Ignore connection parameters in connection.properties file and use Hikari CP:
    SnowflakeJdbcDebugApp false true

5.) After the application runs, it will generate a log file called 
    snowflake_jdbc.log in the same directory where the scripts reside.
