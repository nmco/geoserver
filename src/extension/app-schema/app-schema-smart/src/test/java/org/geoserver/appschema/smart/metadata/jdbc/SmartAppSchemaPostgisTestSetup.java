package org.geoserver.appschema.smart.metadata.jdbc;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;
import org.geoserver.test.onlineTest.setup.ReferenceDataPostgisSetup;
import org.geotools.jdbc.JDBCDataStore;

/**
 * Implementation of ReferenceDataPostgisSetup for SmartAppSchema Postgis tests.
 *
 * @author Jose Macchi - GeoSolutions
 */
public class SmartAppSchemaPostgisTestSetup extends ReferenceDataPostgisSetup {

    private String sql;
    public static final String ONLINE_DB_SCHEMA = "smartappschematest";

    public static SmartAppSchemaPostgisTestSetup getInstance(String sql) throws Exception {
        return new SmartAppSchemaPostgisTestSetup(sql);
    }

    public SmartAppSchemaPostgisTestSetup(String sql) throws Exception {
        super();
        this.sql = sql;
    }

    protected void runSqlInsertScript() throws Exception {
        this.run(sql, false);
    }

    @Override
    public void tearDown() throws Exception {
        dropSchema();
        getDataSource().getConnection().close();
    }

    private void dropSchema() throws Exception {
        Connection conn = getDataSource().getConnection();
        Statement st = conn.createStatement();
        String sql = "DROP SCHEMA IF EXISTS " + ONLINE_DB_SCHEMA + " CASCADE;";
        st.execute(sql);
    }

    public void setUpDataStore(JDBCDataStore dataStore) {
        super.setUpDataStore(dataStore);
        ;
    }

    public Properties createOfflineFixture() {
        return super.createOfflineFixture();
    }

    public Properties createExampleFixture() {
        return super.createExampleFixture();
    }

    public String getDatabaseID() {
        return "postgis";
    }
}
