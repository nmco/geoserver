package org.geoserver.gwc.wmts.dimensions;

import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.DimensionInfo;
import org.geoserver.catalog.DimensionPresentation;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.util.ISO8601Formatter;
import org.geoserver.wms.WMS;
import org.geotools.util.Converters;
import org.geotools.util.DateRange;
import org.geotools.util.NumberRange;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

public final class DimensionsUtils {

    /**
     * Helper method that will extract a layer dimensions.
     */
    public static List<Dimension> extractDimensions(WMS wms, LayerInfo layerInfo) {
        ResourceInfo resourceInfo = layerInfo.getResource();
        if (resourceInfo instanceof FeatureTypeInfo) {
            return extractDimensions(wms, layerInfo, (FeatureTypeInfo) resourceInfo);
        }
        if (resourceInfo instanceof CoverageInfo) {
            return extractDimensions(wms, layerInfo, (CoverageInfo) resourceInfo);
        }
        return Collections.emptyList();
    }

    /**
     * Helper method that will extract the dimensions from a feature type info..
     */
    public static List<Dimension> extractDimensions(WMS wms, LayerInfo layerInfo, FeatureTypeInfo typeInfo) {
        List<Dimension> dimensions = new ArrayList<>();
        DimensionInfo timeDimension = typeInfo.getMetadata().get(ResourceInfo.TIME, DimensionInfo.class);
        if (timeDimension != null) {
            dimensions.add(new VectorTimeDimension(wms, layerInfo, timeDimension));
        }
        DimensionInfo elevationDimension = typeInfo.getMetadata().get(ResourceInfo.ELEVATION, DimensionInfo.class);
        if (elevationDimension != null) {
            dimensions.add(new VectorElevationDimension(wms, layerInfo, elevationDimension));
        }
        return dimensions;
    }

    /**
     * Helper method that will extract the dimensions from a coverage type info..
     */
    public static List<Dimension> extractDimensions(WMS wms, LayerInfo layerInfo, CoverageInfo typeInfo) {
        List<Dimension> dimensions = new ArrayList<>();
        for (Map.Entry<String, Serializable> entry : typeInfo.getMetadata().entrySet()) {
            String key = entry.getKey();
            Serializable value = entry.getValue();
            if (key.equals(ResourceInfo.TIME)) {
                dimensions.add(new RasterTimeDimension(wms, layerInfo, Converters.convert(value, DimensionInfo.class)));
            } else if (key.equals(ResourceInfo.ELEVATION)) {
                dimensions.add(new RasterElevationDimension(wms, layerInfo, Converters.convert(value, DimensionInfo.class)));
            } else if (value instanceof DimensionInfo) {
                DimensionInfo dimensionInfo = (DimensionInfo) value;
                if (dimensionInfo.isEnabled()) {
                    if (key.startsWith(ResourceInfo.CUSTOM_DIMENSION_PREFIX)) {
                        String dimensionName = key.substring(ResourceInfo.CUSTOM_DIMENSION_PREFIX.length());
                        dimensions.add(new RasterCustomDimension(wms, layerInfo, dimensionName, dimensionInfo));
                    }
                }
            }
        }
        return dimensions;
    }

    @SuppressWarnings("unchecked")
    public static List<String> getElevationDomainValuesAsStrings(DimensionInfo dimension, TreeSet<?> values) {
        List<String> stringValues = new ArrayList<>();
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        if (DimensionPresentation.LIST == dimension.getPresentation()) {
            for (Object val : values) {
                if (val instanceof Double) {
                    stringValues.add(val.toString());
                } else {
                    NumberRange<Double> range = (NumberRange<Double>) val;
                    stringValues.add(range.getMinimum() + "--" + range.getMaximum());
                }
            }
        } else {
            NumberRange<Double> range = getMinMaxElevationInterval(values);
            stringValues.add(range.getMinimum() + "--" + range.getMaximum());
        }
        return stringValues;
    }

    public static List<String> getTimeDomainValuesAsStrings(DimensionInfo dimension, TreeSet<?> values) {
        List<String> stringValues = new ArrayList<>();
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        final ISO8601Formatter df = new ISO8601Formatter();
        if (DimensionPresentation.LIST == dimension.getPresentation()) {
            stringValues.addAll(values.stream().map(df::format).collect(Collectors.toList()));
        } else {
            DateRange range = getMinMaxTimeInterval(values);
            stringValues.add(df.format(range.getMinValue()) + "--" + df.format(range.getMaxValue()));
        }
        return stringValues;
    }

    public static List<String> getCustomDomainValuesAsStrings(DimensionInfo dimension, TreeSet<?> values) {
        List<String> stringValues = new ArrayList<>();
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        if (DimensionPresentation.LIST == dimension.getPresentation()) {
            stringValues.addAll(values.stream().map(value -> value.toString().trim()).collect(Collectors.toList()));
        } else if (DimensionPresentation.DISCRETE_INTERVAL == dimension.getPresentation()) {
            stringValues.add(values.first() + "--" + values.last());
        }
        return stringValues;
    }

    public static DateRange getMinMaxTimeInterval(TreeSet<?> values) {
        Object minValue = values.first();
        Object maxValue = values.last();
        Date min, max;
        if (minValue instanceof DateRange) {
            min = ((DateRange) minValue).getMinValue();
        } else {
            min = (Date) minValue;
        }
        if (maxValue instanceof DateRange) {
            max = ((DateRange) maxValue).getMaxValue();
        } else {
            max = (Date) maxValue;
        }
        return new DateRange(min, max);
    }

    @SuppressWarnings("unchecked")
    public static NumberRange<Double> getMinMaxElevationInterval(TreeSet<?> values) {
        Object minValue = values.first();
        Object maxValue = values.last();
        Double min, max;
        if (minValue instanceof NumberRange) {
            min = ((NumberRange<Double>) minValue).getMinValue();
        } else {
            min = (Double) minValue;
        }
        if (maxValue instanceof NumberRange) {
            max = ((NumberRange<Double>) maxValue).getMaxValue();
        } else {
            max = (Double) maxValue;
        }
        return new NumberRange<>(Double.class, min, max);
    }
}
