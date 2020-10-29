package org.geoserver.appschema.smart.metadata.jdbc;

import java.sql.Connection;

/**
 * Common interface used by all the metadata objects in a JDBC DataStore implementation.
 *
 * @author Jose Macchi - Geosolutions
 */
public interface JdbcConnectable {

    public Connection getConnection();
}
