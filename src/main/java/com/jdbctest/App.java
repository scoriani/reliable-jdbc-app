package com.jdbctest;

import java.io.*;

import java.sql.*;

import java.util.Date;

import java.text.SimpleDateFormat;

import java.util.logging.*;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App implements Runnable {

    // Getting credentials
    private static String serverName = System.getenv("servername") + ".database.windows.net";;   
    private static String databaseName = System.getenv("dbname");    
    private static String userName = System.getenv("username"); 
    private static String password = System.getenv("password");
 
    static BufferedReader in ; 
    static boolean quit=false;

    // Exit when user press Q
    public void run(){
        String msg = null;
         
        while(true) {
            try {
                msg=in.readLine();
            }
            catch(IOException e) {
                e.printStackTrace();
                }             
            if(msg.equals("Q")) {quit=true;break;}
        }
    }

    // Get DataSource object from Hikari config
    public static DataSource getDataSource() {
        HikariConfig config = new HikariConfig();
        // Mimic ADO.NET connection pool default
        config.setMinimumIdle(0);
        config.setMaximumPoolSize(100);
        config.setDataSourceClassName("com.microsoft.sqlserver.jdbc.SQLServerDataSource");
        config.addDataSourceProperty("serverName", serverName);
        config.addDataSourceProperty("databaseName", databaseName);
        config.addDataSourceProperty("user", userName);
        config.addDataSourceProperty("password", password);
        // socketTimeout significantly longer than average query response
        config.addDataSourceProperty("socketTimeout", 8000);
        // timeout for getting a connection from the pool
        config.setConnectionTimeout(15000);
        // max lifetime of a connection in the pool
        config.setMaxLifetime(30000);
        // check if a connection is still alive
        config.setValidationTimeout(5000);

        return new HikariDataSource(config);  
    }

    public static void main(String[] args) {

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");  
        Boolean running = true;

        // Logging stuff
        LogManager logManager = LogManager.getLogManager();        
        try {
            InputStream is = new FileInputStream(".\\src\\main\\resources\\logging.properties");
            logManager.readConfiguration(is);
        } catch (Exception e) {
            System.out.println("Load logging configuration...");
            e.printStackTrace();
        }    
        
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger("com.microsoft.sqlserver.jdbc");
        logger.setLevel(Level.FINEST);    
        logger.info("JDBC logger started...");    
            
        Logger logger2 = LoggerFactory.getLogger(App.class);       
        logger2.info("Generic logger started...");

        // Reading console keys to exit
        in=new BufferedReader(new InputStreamReader(System.in));

        Thread t1=new Thread(new App());
        t1.start();
        
        try {
            
            DataSource ds = getDataSource();
            String selectSql = "EXEC spJustWait 5";

            while(running) {
                                                    
                try (Connection connection = ds.getConnection();
                        Statement statement = connection.createStatement();) {

                        ResultSet resultSet = statement.executeQuery(selectSql);

                        while (resultSet.next())
                        {
                            System.out.println(resultSet.getString(1));
                        }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
        
                Thread.sleep(1000);

                if(quit==true) break;

                System.out.println(formatter.format(new Date().getTime()) +  " - Looping....");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}