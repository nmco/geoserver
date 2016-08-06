/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.gwc.wmts.dimensions;

import org.geoserver.catalog.DimensionDefaultValueSetting;
import org.geoserver.catalog.DimensionInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.gwc.wmts.FilteredFeatureType;
import org.geoserver.gwc.wmts.Tuple;
import org.geoserver.wms.WMS;
import org.opengis.filter.Filter;

import java.util.List;
import java.util.TreeSet;

public class VectorTimeDimension extends Dimension {

    public VectorTimeDimension(WMS wms, LayerInfo layerInfo, DimensionInfo dimensionInfo) {
        super(wms, ResourceInfo.TIME, layerInfo, dimensionInfo);
    }

    @Override
    public List<String> getDomainValuesAsStrings(TreeSet<?> domainValues) {
        return DimensionsUtils.getTimeDomainValuesAsStrings(getDimensionInfo(), domainValues);
    }

    @Override
    protected String getDefaultValueFallbackAsString() {
        return DimensionDefaultValueSetting.TIME_CURRENT;
    }

    @Override
    public Tuple<Integer, TreeSet<?>> getDomainValues(Filter filter) {
        try {
            FeatureTypeInfo typeInfo = (FeatureTypeInfo) getResourceInfo();
            TreeSet<?> fullDomainValues = getWms().getFeatureTypeTimes(typeInfo);
            if (filter == null || filter.equals(Filter.INCLUDE)) {
                return Tuple.tuple(fullDomainValues.size(), fullDomainValues);
            }
            TreeSet<?> restrictedValues = getWms().getFeatureTypeTimes(new FilteredFeatureType(typeInfo, filter));
            if (restrictedValues == null) {
                restrictedValues = new TreeSet<>();
            }
            return Tuple.tuple(fullDomainValues.size(), restrictedValues);
        } catch (Exception exception) {
            throw new RuntimeException(String.format(
                    "Error getting domain values for dimension '%s' of coverage '%s'.",
                    getName(), getResourceInfo().getName()), exception);
        }
    }

    @Override
    public Filter getFilter() {
        return buildVectorFilter();
    }
}
