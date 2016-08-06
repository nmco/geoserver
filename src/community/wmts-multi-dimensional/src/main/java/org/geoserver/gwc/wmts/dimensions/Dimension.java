/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.gwc.wmts.dimensions;

import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.DimensionInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.gwc.wmts.Tuple;
import org.geoserver.util.ISO8601Formatter;
import org.geoserver.wms.WMS;
import org.geoserver.wms.dimension.DimensionDefaultValueSelectionStrategy;
import org.geoserver.wms.dimension.DimensionFilterBuilder;
import org.geotools.coverage.grid.io.DimensionDescriptor;
import org.geotools.coverage.grid.io.GranuleSource;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.StructuredGridCoverage2DReader;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.visitor.CountVisitor;
import org.geotools.feature.visitor.UniqueVisitor;
import org.geotools.gce.imagemosaic.properties.time.TimeParser;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.gml2.bindings.GML2EncodingUtils;
import org.geotools.util.DateRange;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

/**
 * This class represents a dimension providing an abstraction
 * layer over all types of dimensions and resources info.
 */
public abstract class Dimension {

    private final WMS wms;
    private final String name;
    private final LayerInfo layerInfo;
    private final DimensionInfo dimensionInfo;

    private final ResourceInfo resourceInfo;

    private final FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();

    private ReferencedEnvelope boundingBox;
    private final List<Object> domainRestrictions = new ArrayList<>();

    public Dimension(WMS wms, String name, LayerInfo layerInfo, DimensionInfo dimensionInfo) {
        this.wms = wms;
        this.name = name;
        this.layerInfo = layerInfo;
        this.dimensionInfo = dimensionInfo;
        resourceInfo = layerInfo.getResource();
    }

    public abstract Tuple<Integer, TreeSet<?>> getDomainValues(Filter filter);

    protected abstract String getDefaultValueFallbackAsString();

    public abstract Filter getFilter();

    public abstract List<String> getDomainValuesAsStrings(TreeSet<?> domainValues);

    public abstract List<String> getHistogram(Filter filter, String resolution);

    protected WMS getWms() {
        return wms;
    }

    protected ResourceInfo getResourceInfo() {
        return resourceInfo;
    }

    public String getName() {
        return name;
    }

    protected DimensionInfo getDimensionInfo() {
        return dimensionInfo;
    }

    protected ReferencedEnvelope getBoundingBox() {
        return boundingBox;
    }

    protected List<Object> getDomainRestrictions() {
        return domainRestrictions;
    }

    public FilterFactory getFilterFactory() {
        return filterFactory;
    }

    public void setBoundingBox(ReferencedEnvelope boundingBox) {
        this.boundingBox = boundingBox;
    }

    public void addDomainRestriction(Object domainRestriction) {
        if (domainRestriction instanceof Collection) {
            domainRestrictions.addAll((Collection) domainRestriction);
        } else {
            domainRestrictions.add(domainRestriction);
        }
    }

    public String getDefaultValueAsString() {
        DimensionDefaultValueSelectionStrategy strategy = wms.getDefaultValueStrategy(resourceInfo, name, dimensionInfo);
        String defaultValue = strategy.getCapabilitiesRepresentation(resourceInfo, name, dimensionInfo);
        return defaultValue != null ? defaultValue : getDefaultValueFallbackAsString();
    }

    protected Filter buildVectorFilter() {
        FeatureTypeInfo typeInfo = (FeatureTypeInfo) getResourceInfo();
        Filter filter = Filter.INCLUDE;
        if (getBoundingBox() != null) {
            String geometryAttributeName;
            try {
                geometryAttributeName = typeInfo.getFeatureSource(null, null).getSchema().getGeometryDescriptor().getLocalName();
            } catch (IOException exception) {
                throw new RuntimeException(String.format("Exception accessing feature source of vector type '%s'.",
                        typeInfo.getName()), exception);
            }
            filter = appendBoundingBoxFilter(filter, geometryAttributeName);
        }
        if (getDomainRestrictions() != null) {
            filter = appendDomainRestrictionsFilter(filter);
        }
        return filter;
    }

    private Filter appendBoundingBoxFilter(Filter filter, String geometryAttributeName) {
        CoordinateReferenceSystem coordinateReferenceSystem = getBoundingBox().getCoordinateReferenceSystem();
        String epsgCode = coordinateReferenceSystem == null ? null : GML2EncodingUtils.toURI(coordinateReferenceSystem);
        Filter spatialFilter = getFilterFactory().bbox(geometryAttributeName, getBoundingBox().getMinX(), getBoundingBox().getMinY(),
                getBoundingBox().getMaxX(), getBoundingBox().getMaxY(), epsgCode);
        return getFilterFactory().and(filter, spatialFilter);
    }

    private Filter appendDomainRestrictionsFilter(Filter filter) {
        DimensionFilterBuilder dimensionFilterBuilder = new DimensionFilterBuilder(getFilterFactory());
        dimensionFilterBuilder.appendFilters(getDimensionInfo().getAttribute(),
                getDimensionInfo().getEndAttribute(), getDomainRestrictions());
        return getFilterFactory().and(filter, dimensionFilterBuilder.getFilter());
    }

    protected Filter buildRasterFilter() {
        CoverageInfo typeInfo = (CoverageInfo) getResourceInfo();
        Filter filter = Filter.INCLUDE;
        if (getBoundingBox() != null) {
            try {
                filter = appendBoundingBoxFilter(filter, typeInfo);
            } catch (IOException exception) {
                throw new RuntimeException(String.format("Exception accessing feature source of raster type '%s'.",
                        typeInfo.getName()), exception);
            }
        }
        if (getDomainRestrictions() != null) {
            filter = appendDomainRestrictionsFilter(filter);
        }
        return filter;
    }

    private Filter appendBoundingBoxFilter(Filter filter, CoverageInfo typeInfo) throws IOException {
        GridCoverage2DReader reader = (GridCoverage2DReader) typeInfo.getGridCoverageReader(null, null);
        if (!(reader instanceof StructuredGridCoverage2DReader)) {
            return filter;
        }
        StructuredGridCoverage2DReader structuredReader = (StructuredGridCoverage2DReader) reader;
        String coverageName = structuredReader.getGridCoverageNames()[0];
        GranuleSource source = structuredReader.getGranules(coverageName, true);
        String geometryAttributeName = source.getSchema().getGeometryDescriptor().getLocalName();
        return appendBoundingBoxFilter(filter, geometryAttributeName);
    }

    protected Tuple<Integer, TreeSet<?>> getRasterDomainValues(Filter filter) throws IOException {
        CoverageInfo typeInfo = (CoverageInfo) getResourceInfo();
        GridCoverage2DReader reader = (GridCoverage2DReader) typeInfo.getGridCoverageReader(null, null);
        if (!(reader instanceof StructuredGridCoverage2DReader)) {
            throw new RuntimeException("Non structured grid coverages cannot be filtered.");
        }
        StructuredGridCoverage2DReader structuredReader = (StructuredGridCoverage2DReader) reader;
        String coverageName = structuredReader.getGridCoverageNames()[0];
        GranuleSource source = structuredReader.getGranules(coverageName, true);
        List<DimensionDescriptor> descriptors = structuredReader.getDimensionDescriptors(name);
        for (DimensionDescriptor descriptor : descriptors) {
            if (getName().equalsIgnoreCase(descriptor.getName()) && descriptor.getEndAttribute() == null) {
                Query query = new Query(source.getSchema().getName().getLocalPart());
                query.setFilter(filter);
                FeatureCollection featureCollection = source.getGranules(query);
                UniqueVisitor uniqueVisitor = new UniqueVisitor(dimensionInfo.getAttribute());
                featureCollection.accepts(uniqueVisitor, null);
                CountVisitor countvisitor = new CountVisitor();
                featureCollection.accepts(countvisitor, null);
                return Tuple.tuple(countvisitor.getCount(), new TreeSet(uniqueVisitor.getUnique()));
            }
        }
        return Tuple.tuple(0, new TreeSet());
    }

    protected Tuple<List<String>, List<String>> getHistogramForTimeValues(Filter filter, String resolution) {
        Tuple<Integer, TreeSet<?>> domainValues = getDomainValues(filter);
        DateRange minMax = DimensionsUtils.getMinMaxTimeInterval(domainValues.second);
        ISO8601Formatter dateFormatter = new ISO8601Formatter();
        String domainString = dateFormatter.format(minMax.getMinValue()) +
                "/" + dateFormatter.format(minMax.getMaxValue()) + "/" + resolution;
        TimeParser timeParser = new TimeParser();
        List<Date> intervals = timeParser.parse(domainString);
        List<DateRange> buckets = new ArrayList<>();
        if (intervals.size() == 1) {
            buckets.add(new DateRange(intervals.get(0), intervals.get(0)));
        } else {
            Date previous = intervals.get(0);
            for (int i = 1; i < intervals.size(); i++) {
                buckets.add(new DateRange(previous, intervals.get(i)));
                previous = intervals.get(i);
            }
        }
        int[] histogram = new int[buckets.size()];
    }
}
