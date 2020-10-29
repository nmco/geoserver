package org.geoserver.appschema.smart.domain.entities;

import java.util.ArrayList;
import java.util.List;
import org.geoserver.appschema.smart.domain.DomainModelVisitor;

/**
 * Class representing an entity on the Smart AppSchema model.
 *
 * @author Jose Macchi - GeoSolutions
 */
public final class DomainEntity {

    private String name;
    private final List<DomainEntitySimpleAttribute> attributes = new ArrayList<>();
    private final List<DomainRelation> relations = new ArrayList<>();

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DomainEntitySimpleAttribute> getAttributes() {
        return attributes;
    }

    public List<DomainRelation> getRelations() {
        return relations;
    }

    public void add(DomainEntitySimpleAttribute attribute) {
        attributes.add(attribute);
    }

    public void add(DomainRelation relation) {
        relations.add(relation);
    }

    public void accept(DomainModelVisitor visitor, boolean isRoot) {
        if (isRoot) visitor.visitDomainRootEntity(this);
        else visitor.visitDomainChainedEntity(this);
        this.getAttributes()
                .forEach(
                        attrib -> {
                            attrib.accept(visitor);
                        });
        this.getRelations()
                .forEach(
                        relation -> {
                            relation.accept(visitor);
                        });
    }
}
