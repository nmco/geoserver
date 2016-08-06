/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.gwc.wmts.dimensions;

import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.DimensionDefaultValueSetting;
import org.geoserver.catalog.DimensionInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.util.ReaderDimensionsAccessor;
import org.geoserver.gwc.wmts.Tuple;
import org.geoserver.wms.WMS;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.opengis.filter.Filter;

import java.io.IOException;
import java.util.List;
import java.util.TreeSet;

public class RasterElevationDimension extends Dimension {

    public RasterElevationDimension(WMS wms, LayerInfo layerInfo, DimensionInfo dimensionInfo) {
        super(wms, ResourceInfo.ELEVATION, layerInfo, dimensionInfo);
    }

    @Override
    public List<String> getDomainValuesAsStrings(TreeSet<?> domainValues) {
        return DimensionsUtils.getElevationDomainValuesAsStrings(getDimensionInfo(), domainValues);
    }

    @Override
    protected String getDefaultValueFallbackAsString() {
        return DimensionDefaultValueSetting.TIME_CURRENT;
    }

    @Override
    public Tuple<Integer, TreeSet<?>> getDomainValues(Filter filter) {
        try {
            CoverageInfo typeInfo = (CoverageInfo) getResourceInfo();
            GridCoverage2DReader reader = (GridCoverage2DReader) typeInfo.getGridCoverageReader(null, null);
            ReaderDimensionsAccessor dimensions = new ReaderDimensionsAccessor(reader);
            if (filter == null || filter.equals(Filter.INCLUDE)) {
                TreeSet<?> values = dimensions.getElevationDomain();
                return Tuple.tuple(values.size(), values);
            }
            return getRasterDomainValues(filter);
        } catch (IOException exception) {
            throw new RuntimeException(String.format("Error getting domain values for dimension '%s' of coverage '%s'.",
                    getName(), getResourceInfo().getName()), exception);
        }
    }

    @Override
    public Filter getFilter() {
        return buildRasterFilter();
    }
}
