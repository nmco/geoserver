/* (c) 2017 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.nsg;

import org.geoserver.catalog.FeatureTypeInfo;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.filter.visitor.DuplicatingFilterVisitor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Id;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.identity.Identifier;
import org.opengis.filter.identity.ResourceId;
import org.opengis.filter.sort.SortOrder;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

final class VersioningFilterAdapter extends DuplicatingFilterVisitor {

    private final String idPropertyName;
    private final String timePropertyName;

    private VersioningFilterAdapter(FeatureTypeInfo featureTypeInfo) {
        this.idPropertyName = TimeVersioning.getIdPropertyName(featureTypeInfo);
        this.timePropertyName = TimeVersioning.getTimePropertyName(featureTypeInfo);
    }

    @Override
    public Object visit(Id filter, Object extraData) {
        FilterFactory filterFactory = getFactory(extraData);
        Set<Identifier> ids = filter.getIdentifiers();
        Set<Identifier> finalIds = new HashSet<>();
        Filter versioningFilter = null;
        for (Identifier id : ids) {
            if (id instanceof ResourceId) {
                Filter newFilter = buildVersioningFilter(filterFactory, (ResourceId) id);
                versioningFilter = addFilter(filterFactory, versioningFilter, newFilter);
            } else {
                finalIds.add(id);
            }
        }
        if (finalIds.isEmpty()) {
            return versioningFilter;
        }
        Filter newIdFilter = getFactory(extraData).id(finalIds);
        if (versioningFilter != null) {
            return filterFactory.and(newIdFilter, versioningFilter);
        }
        return newIdFilter;
    }

    private Filter buildVersioningFilter(FilterFactory filterFactory, ResourceId resourceId) {
        Filter idFilter = buildIdFilter(filterFactory, resourceId.getID());
        Filter timeFilter = buildTimeFilter(filterFactory, resourceId.getStartTime(), resourceId.getEndTime());
        if (idFilter != null && timeFilter != null) {
            return filterFactory.and(idFilter, timeFilter);
        }
        if (idFilter != null) {
            return idFilter;
        }
        if (timeFilter != null) {
            return timeFilter;
        }
        return null;
    }

    private Filter buildIdFilter(FilterFactory factory, String id) {
        if (id == null) {
            return null;
        }
        return factory.equals(factory.property(idPropertyName), factory.literal(id));
    }

    private Filter buildTimeFilter(FilterFactory filterFactory, Date start, Date end) {
        Expression timeProperty = filterFactory.property(timePropertyName);
        Expression startLiteral = filterFactory.literal(start);
        Expression endLiteral = filterFactory.literal(end);
        Filter after = filterFactory.after(timeProperty, startLiteral);
        Filter before = filterFactory.before(timeProperty, endLiteral);
        if (start != null && end != null) {
            return filterFactory.and(after, before);
        }
        if (start != null) {
            return after;
        }
        if (end != null) {
            return before;
        }
        return null;
    }

    private Filter addFilter(FilterFactory filterFactory, Filter versioningFilter, Filter filter) {
        if (versioningFilter != null) {
            return filterFactory.and(versioningFilter, filter);
        }
        return filter;
    }

    static Filter adapt(FeatureTypeInfo featureTypeInfo, Filter filter) {
        String timePropertyName = TimeVersioning.getTimePropertyName(featureTypeInfo);
        VersioningFilterAdapter adapter = new VersioningFilterAdapter(featureTypeInfo);
        return (Filter) filter.accept(adapter, null);
    }
}
