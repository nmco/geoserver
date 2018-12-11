/* (c) 2019 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.hive.jdbc.store;

import java.io.IOException;
import java.util.Map;
import org.apache.commons.dbcp.BasicDataSource;
import org.geotools.data.Parameter;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.SQLDialect;

public final class HiveDataStoreFactory extends JDBCDataStoreFactory {

    // database type parameter parameter
    private static final Param DB_TYPE =
            new Param("dbtype", String.class, "Type", true, "hive2", Parameter.LEVEL, "program");

    // hive database
    private static final Param HIVE_DATA_BASE =
            new Param("database", String.class, "Database", true, "default");

    // hive configuration parameter
    private static final Param HIVE_CONF =
            new Param(
                    "Hive configuration",
                    String.class,
                    "Hive configuration",
                    false,
                    null,
                    Parameter.IS_LARGE_TEXT,
                    true);

    @Override
    protected void setupParameters(Map parameters) {
        // add the default parameters
        super.setupParameters(parameters);
        // hive mandatory parameters
        parameters.put(DB_TYPE.key, DB_TYPE);
        parameters.put(HOST.key, HOST);
        parameters.put(PORT.key, PORT);
        parameters.put(HIVE_DATA_BASE.key, HIVE_DATA_BASE);
        // hive optional configuration
        parameters.put(HIVE_CONF.key, HIVE_CONF);
    }

    @Override
    public String getDescription() {
        return "Provides access to large data sets residing "
                + "in a distributed storage using SQL.";
    }

    @Override
    public String getDisplayName() {
        return "Hive JDBC";
    }

    @Override
    protected String getDatabaseID() {
        return "hive2";
    }

    @Override
    protected String getDriverClassName() {
        return "org.apache.hive.jdbc.HiveDriver";
    }

    @Override
    protected SQLDialect createSQLDialect(JDBCDataStore dataStore) {
        return new HiveSqlDialect(dataStore);
    }

    @Override
    protected String getValidationQuery() {
        return "select 1";
    }

    @Override
    protected JDBCDataStore createDataStoreInternal(JDBCDataStore dataStore, Map params)
            throws IOException {
        return dataStore;
    }

    @Override
    public BasicDataSource createDataSource(Map parameters) throws IOException {
        // create apache dbcp basic data source
        BasicDataSource dataSource = new BasicDataSource();
        // set hive driver name
        dataSource.setDriverClassName(getDriverClassName());
        // build the jdbc url using the provided parameters
        dataSource.setUrl(buildJdbcUrl(parameters));
        // we may need to access native objects
        dataSource.setAccessToUnderlyingConnectionAllowed(true);
        return dataSource;
    }

    /** Builds Hive JDBC URL using the provided connection parameters and properties. */
    private String buildJdbcUrl(Map parameters) throws IOException {
        // build the basic jdbc url
        String url = super.getJDBCUrl(parameters);
        // add authentication
        url += buildAuthenticationString(parameters);
        // add hive specific configuration properties
        String hiveConf = (String) HIVE_CONF.lookUp(parameters);
        return hiveConf == null ? url : url + hiveConf;
    }
    
    private String buildAuthenticationString(Map parameters) throws IOException {
        String authentication = ";";
        String user = (String) USER.lookUp(parameters);
        if (user != null) {
            authentication += "user=" + user + ";";
        }
        String password = (String) PASSWD.lookUp(parameters);
        if (password == null) {
            return authentication;
        }
        return authentication +"password=" + password + ";";
    }

    /**
     * Helper method that parses Hive configuration properties string ([key1>]=[value1];
     * [key2]=[value2];...) and register the found properties as connection properties.
     */
    private static void addConnectionProperties(BasicDataSource dataSource, String rawHiveConfig) {
        if (rawHiveConfig == null || rawHiveConfig.isEmpty()) {
            // no hive configuration properties, nothing to do
            return;
        }
        // parse the connection properties
        String[] hiveParameters = rawHiveConfig.split(";");
        for (String parameter : hiveParameters) {
            // parse the parameter name and value
            String[] parts = parameter.split("=");
            if (parts.length != 2) {
                // this is not a valid hive configuration property
                throw new RuntimeException(
                        "Invalid Hive configuration properties, "
                                + "the format should be: [key1>]=[value1];[key2]=[value2];...");
            }
            // add the connection parameter
            dataSource.addConnectionProperty("hiveconf:" + parts[0], parts[1]);
        }
    }
}
