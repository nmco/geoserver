package org.geoserver.appschema.smart.metadata.jdbc.utils;

import org.geoserver.appschema.smart.metadata.jdbc.JdbcColumnMetadata;

/**
 * Helper for testing purposes. Represent columns results extracted from JDBC dataStores (metadata
 * access using JdbcHelper).
 *
 * @author Jose Macchi - GeoSolutions
 */
public class ResultColumn {
    private final JdbcColumnMetadata jdbcColumn;

    public ResultColumn(JdbcColumnMetadata aColumn) {
        this.jdbcColumn = aColumn;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(jdbcColumn.getEntity().getName());
        stringBuilder.append(" - ");
        stringBuilder.append(jdbcColumn.getName());
        stringBuilder.append(" (");
        stringBuilder.append(jdbcColumn.getType());
        stringBuilder.append(")");
        return stringBuilder.toString();
    }
}
