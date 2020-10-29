package org.geoserver.appschema.smart.metadata.jdbc.constraint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.geoserver.appschema.smart.metadata.jdbc.JdbcTableMetadata;

/**
 * Class representing metadata for a constraint type primarykey in a JDBC DataStore. A primary key
 * can be composed by more than one column.
 *
 * @author Jose Macchi - Geosolutions
 */
public class JdbcPrimaryKeyConstraintMetadata extends JdbcTableConstraintMetadata {
    private final List<String> columnNames;
    private final Map<String, Integer> columnOrderMap;

    public JdbcPrimaryKeyConstraintMetadata(
            JdbcTableMetadata table, String constraintName, List<String> columnNames) {
        super(table, constraintName);
        this.columnNames = columnNames;
        this.columnOrderMap = new HashMap<String, Integer>();
        for (int i = 0; i < columnNames.size(); i++) {
            columnOrderMap.put(columnNames.get(i), i);
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(this.getTable().toString());
        stringBuilder.append("(");
        for (String columnName : columnNames) {
            stringBuilder.append(columnName);
            stringBuilder.append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || !(object instanceof JdbcPrimaryKeyConstraintMetadata)) {
            return false;
        }
        JdbcPrimaryKeyConstraintMetadata primaryKey = (JdbcPrimaryKeyConstraintMetadata) object;
        return this.compareTo(primaryKey) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getTable(), this.getName());
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public Map<String, Integer> getColumnOrderMap() {
        return columnOrderMap;
    }
}
