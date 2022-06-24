//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import java.io.*;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import net.snowflake.client.jdbc.SnowflakeResultSet;
import java.security.Security;
import java.security.PrivateKey;

import net.snowflake.client.jdbc.internal.apache.commons.codec.binary.Base64;
import net.snowflake.client.jdbc.internal.org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;

public class SnowflakeJdbcDebugApp {
    private static final String JDBC_PRIVATE_KEY_FILE="private_key_file";
    private static final String JDBC_PRIVATE_KEY = "privateKey";
    private static final String JDBC_PRIVATE_KEY_PASSPHRASE="private_key_file_pwd";
    private static String KEY_PASSPHRASE = null;
    private static long connectionStartTime;
    private static long connectionEndTime;
    private static long connectionDuration;
    private static Connection connection;
    private static String[] sqlQueries;
    static boolean useConnectionParameters = false;

    public SnowflakeJdbcDebugApp() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            useConnectionParameters = true;
        }

        String queryId = null;
        getSqlQueries();
        connection = getConnection();
        connectionEndTime = System.nanoTime();
        connectionDuration = connectionEndTime - connectionStartTime;
        System.out.println("DebugApp: Finished getting Snowflake connection in: " + connectionDuration / 1000000L + " ms.");
        Statement statement = connection.createStatement();
        long executeTotalTime = 0L;
        long executeStartTime = 0L;
        long executeEndTime = 0L;
        long fetchTotalTime = 0L;
        long fetchStartTime = 0L;
        long fetchEndTime = 0L;

        String[] sqlStatements = sqlQueries;

        for(int i = 0; i < sqlStatements.length; ++i) {
            String sqlQuery = sqlStatements[i];
            executeStartTime = System.nanoTime();
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            executeEndTime = System.nanoTime();
            executeTotalTime = executeEndTime - executeStartTime;
            queryId = ((SnowflakeResultSet)resultSet.unwrap(SnowflakeResultSet.class)).getQueryID();
            System.out.println("Query ID: " + queryId);
            System.out.println("______________________");
            System.out.println("Total execution time: " + executeTotalTime / 1000000L + " ms.");
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int colCount = resultSetMetaData.getColumnCount();

            int rowIdx;
            for(rowIdx = 0; rowIdx < colCount; ++rowIdx) {
                System.out.println("Column " + rowIdx + ": type=" + resultSetMetaData.getColumnTypeName(rowIdx + 1));
            }

            System.out.println("\nData:");
            System.out.println("================================");
            rowIdx = 0;

            fetchStartTime = System.nanoTime();
            while(resultSet.next()) {
                for(int j = 0; j < colCount; j++) {
                    System.out.println("row " + rowIdx + ", column " + j + ": " + resultSet.getString(j + 1));
                }
                rowIdx++;
            }

            fetchEndTime = System.nanoTime();
            fetchTotalTime = fetchEndTime - fetchStartTime;
            System.out.println("Total time spent fetching data: " + fetchTotalTime / 1000000L + " ms.");
            System.out.println("Number of rows fetched: " + rowIdx);
            resultSet.close();
        }

        statement.close();
        connection.close();
    }

    private static Connection getConnection() throws Exception {
        Properties properties = new Properties();
        Properties connectionProps = new Properties();
        StringBuilder sb = new StringBuilder();

        FileReader reader;
        try {
            reader = new FileReader("snowflake.properties");
            properties.load(reader);
        } catch (IOException ex) {
            System.out.println("Failed to load snowflake.properties file: " + ex.getMessage());
            System.out.println(ex.getStackTrace());
            System.exit(1);
        }

        //Check if key pair authentication is used
        if(isKeyPairAuthenticationUsed(properties)){
            //If a password is provided then we assume the key is encrypted
            if(isPrivateKeyEncrypted(properties)){
                KEY_PASSPHRASE = properties.getProperty(JDBC_PRIVATE_KEY_PASSPHRASE);
                String file = properties.getProperty(JDBC_PRIVATE_KEY_FILE);
                properties.put(JDBC_PRIVATE_KEY, PrivateKeyReader.get(file));
                properties.remove(JDBC_PRIVATE_KEY_FILE);
            }

        }

        if (useConnectionParameters) {
            try {
                reader = new FileReader("connection.properties");
                connectionProps.load(reader);
                Set<String> connectionParams = connectionProps.stringPropertyNames();
                sb.append("?");
                Iterator connectionParameter = connectionParams.iterator();

                while(connectionParameter.hasNext()) {
                    String key = (String)connectionParameter.next();
                    sb.append("&" + key + "=" + connectionProps.getProperty(key));
                }
            } catch (IOException ex) {
                System.out.println("Failed to load connection.properties file: " + ex.getMessage());
                System.out.println(ex.getStackTrace());
                System.exit(1);
            }
        }

        String ACCOUNT_URL = properties.getProperty("ACCOUNT_URL");
        if (ACCOUNT_URL.toLowerCase().startsWith("https://")) {
            ACCOUNT_URL = ACCOUNT_URL.substring(ACCOUNT_URL.lastIndexOf("https://") + "https://".length());
            System.out.println("ACCOUNT_URL: " + ACCOUNT_URL);
        }

        String connectStr = "jdbc:snowflake://" + ACCOUNT_URL;
        if (useConnectionParameters) {
            connectStr = "jdbc:snowflake://" + ACCOUNT_URL + sb.toString();
        }

        System.out.println("Using connection string: " + connectStr);
        System.out.println("DebugApp: Creating Snowflake connection");
        connectionStartTime = System.nanoTime();
        return DriverManager.getConnection(connectStr, properties);
    }

    private static void getSqlQueries() {
        StringBuilder sb = new StringBuilder();
        File sqlFile = new File("snowflake.sql");

        try {
            Scanner sc = new Scanner(sqlFile);

            while(sc.hasNextLine()) {
                sb.append(sc.nextLine());
            }

            String data = sb.toString();
            String[] sqlCommands = data.split(";");
            sqlQueries = sqlCommands;
        } catch (FileNotFoundException ex) {
            System.out.println("Failed to load snowflake.properties file: " + ex.getMessage());
            System.out.println(ex.getStackTrace());
            System.exit(1);
        }

    }

    private static boolean isKeyPairAuthenticationUsed(Properties properties){
        boolean enabled = false;
        if(properties.getProperty(JDBC_PRIVATE_KEY_FILE) != null)
            enabled = true;

        return enabled;
    }

    private static boolean isPrivateKeyEncrypted(Properties properties){
        boolean enabled = false;
        if(properties.getProperty(JDBC_PRIVATE_KEY_PASSPHRASE) != null)
            enabled = true;

        return enabled;
    }

    public static class PrivateKeyReader
    {

        // If you generated an encrypted private key, implement this method to return
        // the passphrase for decrypting your private key.
        private static String getPrivateKeyPassphrase() {
            return KEY_PASSPHRASE;
        }

        public static PrivateKey get(String filename)
                throws Exception
        {
            PrivateKeyInfo privateKeyInfo = null;
            Security.addProvider(new BouncyCastleProvider());
            // Read an object from the private key file.
            PEMParser pemParser = new PEMParser(new FileReader(Paths.get(filename).toFile()));
            Object pemObject = pemParser.readObject();
            if (pemObject instanceof PKCS8EncryptedPrivateKeyInfo) {
                // Handle the case where the private key is encrypted.
                PKCS8EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = (PKCS8EncryptedPrivateKeyInfo) pemObject;
                String passphrase = getPrivateKeyPassphrase();
                InputDecryptorProvider pkcs8Prov = new JceOpenSSLPKCS8DecryptorProviderBuilder().build(passphrase.toCharArray());
                privateKeyInfo = encryptedPrivateKeyInfo.decryptPrivateKeyInfo(pkcs8Prov);
            } else if (pemObject instanceof PrivateKeyInfo) {
                // Handle the case where the private key is unencrypted.
                privateKeyInfo = (PrivateKeyInfo) pemObject;
            }
            pemParser.close();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME);
            return converter.getPrivateKey(privateKeyInfo);
        }
    }


}
