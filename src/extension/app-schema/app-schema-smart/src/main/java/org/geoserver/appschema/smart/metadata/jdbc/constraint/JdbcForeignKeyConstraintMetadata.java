package org.geoserver.appschema.smart.metadata.jdbc.constraint;

import com.google.common.collect.ComparisonChain;
import java.util.Objects;
import org.geoserver.appschema.smart.metadata.ConstraintMetadata;
import org.geoserver.appschema.smart.metadata.jdbc.JdbcTableMetadata;

/**
 * Class representing metadata for a constraint type foreignkey in a JDBC DataStore.
 *
 * @author Jose Macchi - Geosolutions
 */
public class JdbcForeignKeyConstraintMetadata extends JdbcTableConstraintMetadata {
    private final JdbcTableMetadata relatedTable;

    public JdbcForeignKeyConstraintMetadata(
            JdbcTableMetadata table, String name, JdbcTableMetadata relatedTable) {
        super(table, name);
        this.relatedTable = relatedTable;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(this.getTable().toString());
        stringBuilder.append(" -> ");
        stringBuilder.append(this.relatedTable);
        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || !(object instanceof JdbcForeignKeyConstraintMetadata)) {
            return false;
        }
        JdbcForeignKeyConstraintMetadata foreignKeyConstraint =
                (JdbcForeignKeyConstraintMetadata) object;
        return this.compareTo(foreignKeyConstraint) == 0;
    }

    @Override
    public int compareTo(ConstraintMetadata tableConstraint) {
        if (tableConstraint != null) {
            JdbcForeignKeyConstraintMetadata tc =
                    (JdbcForeignKeyConstraintMetadata) tableConstraint;
            return ComparisonChain.start()
                    .compare(this.getTable(), tc.getTable())
                    .compare(this.getName(), tc.getName())
                    .compare(this.relatedTable, tc.getRelatedTable())
                    .result();
        }
        return 1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getTable(), this.getName());
    }

    public JdbcTableMetadata getRelatedTable() {
        return relatedTable;
    }

    public boolean referencesTo(JdbcTableMetadata table) {
        if (table != null && (table.equals(relatedTable) || table.equals(this.getTable())))
            return true;
        return false;
    }
}
