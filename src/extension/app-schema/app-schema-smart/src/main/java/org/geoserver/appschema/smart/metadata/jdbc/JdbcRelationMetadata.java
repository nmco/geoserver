package org.geoserver.appschema.smart.metadata.jdbc;

import com.google.common.collect.ComparisonChain;
import java.sql.Connection;
import org.geoserver.appschema.smart.domain.entities.DomainRelationType;
import org.geoserver.appschema.smart.metadata.AttributeMetadata;
import org.geoserver.appschema.smart.metadata.RelationMetadata;

/**
 * Class representing metadata for a relation (RelationMetadata) in a JDBC DataStore. Internally it
 * wraps a JdbcForeignKeyColumnMetadata object.
 *
 * @author Jose Macchi - Geosolutions
 */
public class JdbcRelationMetadata extends RelationMetadata implements JdbcConnectable {

    protected String name;
    private final JdbcForeignKeyColumnMetadata jfkc;

    public JdbcRelationMetadata(
            String name, DomainRelationType type, JdbcForeignKeyColumnMetadata fkColumn) {
        super(type, fkColumn, fkColumn.getRelatedColumn());
        this.name = name;
        this.jfkc = fkColumn;
    }

    @Override
    public int compareTo(RelationMetadata relation) {
        if (relation != null) {
            JdbcRelationMetadata r = (JdbcRelationMetadata) relation;
            return ComparisonChain.start()
                    .compare(this.getSourceAttribute(), r.getSourceAttribute())
                    .compare(this.getDestinationAttribute(), r.getDestinationAttribute())
                    .compare(this.getRelationType(), r.getRelationType())
                    .compare(this.name, r.getName())
                    .result();
        }
        return 1;
    }

    @Override
    public Connection getConnection() {
        return this.jfkc.getConnection();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.name);
        stringBuilder.append(" - ");
        stringBuilder.append(this.getSourceAttribute());
        stringBuilder.append(" <-");
        stringBuilder.append(this.getRelationType());
        stringBuilder.append("-> ");
        stringBuilder.append(this.getDestinationAttribute());
        return stringBuilder.toString();
    }

    @Override
    public AttributeMetadata getSourceAttribute() {
        return this.jfkc;
    }

    @Override
    public AttributeMetadata getDestinationAttribute() {
        return this.jfkc.getRelatedColumn();
    }

    @Override
    public DomainRelationType getRelationType() {
        return this.type;
    }

    public String getName() {
        return name;
    }
}
