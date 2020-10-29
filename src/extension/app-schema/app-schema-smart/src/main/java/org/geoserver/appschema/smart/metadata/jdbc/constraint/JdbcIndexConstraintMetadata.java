package org.geoserver.appschema.smart.metadata.jdbc.constraint;

import java.util.Objects;
import org.geoserver.appschema.smart.metadata.jdbc.JdbcTableMetadata;

/**
 * Class representing metadata for a constraint type index in a JDBC DataStore.
 *
 * @author Jose Macchi - Geosolutions
 */
public class JdbcIndexConstraintMetadata extends JdbcTableConstraintMetadata {

    public JdbcIndexConstraintMetadata(JdbcTableMetadata table, String constraintName) {
        super(table, constraintName);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || !(object instanceof JdbcIndexConstraintMetadata)) {
            return false;
        }
        JdbcIndexConstraintMetadata indexConstraint = (JdbcIndexConstraintMetadata) object;
        return this.compareTo(indexConstraint) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getTable(), this.getName());
    }
}
