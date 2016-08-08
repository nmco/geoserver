/* (c) 2014 - 2016 Open Source Geospatial Foundation - all rights reserved
 * (c) 2001 - 2013 OpenPlans
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wms.capabilities;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.CoverageStoreInfo;
import org.geoserver.catalog.DimensionDefaultValueSetting;
import org.geoserver.catalog.DimensionInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.util.ReaderDimensionsAccessor;
import org.geoserver.platform.ServiceException;
import org.geoserver.wms.WMS;
import org.geoserver.wms.dimension.DimensionDefaultValueSelectionStrategy;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.util.Converters;
import org.geotools.util.DateRange;
import org.geotools.util.NumberRange;
import org.geotools.util.logging.Logging;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class avoiding to duplicate the time/elevation management code between WMS 1.1 and 1.3
 *
 * @author Andrea Aime - GeoSolutions
 */
public abstract class GenericDimensionHelper {

    static final Logger LOGGER = Logging.getLogger(GenericDimensionHelper.class);

    WMS wms;

    public GenericDimensionHelper(WMS wms) {
        this.wms = wms;
    }

    protected abstract void hasDimensionsInfo(DimensionInfo elevInfo, DimensionInfo timeInfo);

    protected abstract void handleTimeDimension(String timeMetadata, String defaultTimeStr);

    protected abstract void handleElevationDimension(TreeSet<?> elevations, String elevationMetadata, String units, String unitSymbol, String defaultValue);

    protected abstract void handleCustomDimension(String name, String metadata, String defaultValue, String unit, String unitSymbol);

    @SuppressWarnings("unchecked")
    protected abstract String getZDomainRepresentation(DimensionInfo dimension, TreeSet<?> values);

    /**
     * Builds the proper presentation given the current
     *
     * @param dimension
     * @param values
     *
     */
    protected abstract String getTemporalDomainRepresentation(DimensionInfo dimension, TreeSet<?> values);

    /**
     * Builds the proper presentation given the specified value domain
     *
     * @param dimension
     * @param values
     *
     */
    protected abstract String getCustomDomainRepresentation(String name, DimensionInfo dimension, List<String> values);

    public void handleVectorLayerDimensions(LayerInfo layer) {
        //TODO: custom dimension handling
        // do we have time and elevation?
        FeatureTypeInfo typeInfo = (FeatureTypeInfo) layer.getResource();
        DimensionInfo timeInfo = typeInfo.getMetadata().get(ResourceInfo.TIME,
                DimensionInfo.class);
        DimensionInfo elevInfo = typeInfo.getMetadata().get(ResourceInfo.ELEVATION,
                DimensionInfo.class);
        boolean hasTime = timeInfo != null && timeInfo.isEnabled();
        boolean hasElevation = elevInfo != null && elevInfo.isEnabled();

        // skip if no need
        if (!hasTime && !hasElevation) {
            return;
        }

        if (hasElevation) {
            hasDimensionsInfo(elevInfo, hasTime ? timeInfo : null);
        }

        // Time dimension
        if (hasTime) {
            try {
                handleTimeDimensionVector(typeInfo);
            } catch (IOException e) {
                throw new RuntimeException("Failed to handle time attribute for layer", e);
            }
        }

        // elevation dimension
        if (hasElevation) {
            try {
                handleElevationDimensionVector(typeInfo);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Writes down the raster layer dimensions, if any
     * 
     * @param layer
     * @throws RuntimeException
     * @throws IOException 
     */
    public void handleRasterLayerDimensions(final LayerInfo layer) throws RuntimeException, IOException {
        
        // do we have time and elevation?
        CoverageInfo cvInfo = (CoverageInfo) layer.getResource();
        if (cvInfo == null)
            throw new ServiceException("Unable to acquire coverage resource for layer: "
                    + layer.getName());

        DimensionInfo timeInfo = null;
        DimensionInfo elevInfo = null;
        Map<String, DimensionInfo> customDimensions = new HashMap<>();
        GridCoverage2DReader reader = null;
        
        for (Map.Entry<String, Serializable> e : cvInfo.getMetadata().entrySet()) {
            String key = e.getKey();
            Object value = e.getValue();
            if (key.equals(ResourceInfo.TIME)) {
                timeInfo = Converters.convert(value, DimensionInfo.class);
            } else if (key.equals(ResourceInfo.ELEVATION)) {
                elevInfo = Converters.convert(value, DimensionInfo.class);
            } else if (value instanceof DimensionInfo) {
                DimensionInfo dimInfo = (DimensionInfo) value;
                if (dimInfo.isEnabled()) {
                    if (key.startsWith(ResourceInfo.CUSTOM_DIMENSION_PREFIX)) {
                        String dimensionName = key.substring(ResourceInfo.CUSTOM_DIMENSION_PREFIX
                                .length());
                        customDimensions.put(dimensionName, dimInfo);
                    } else {
                        LOGGER.log(Level.SEVERE, "Skipping custom  dimension with key " + key
                                + " since it does not start with "
                                + ResourceInfo.CUSTOM_DIMENSION_PREFIX);
                    }
                }
            }
        }
        boolean hasTime = timeInfo != null && timeInfo.isEnabled();
        boolean hasElevation = elevInfo != null && elevInfo.isEnabled();
        boolean hasCustomDimensions = !customDimensions.isEmpty();

        // skip if nothing is configured
        if (!hasTime && !hasElevation && !hasCustomDimensions) {
            return;
        }
        
        Catalog catalog = cvInfo.getCatalog();
        if (catalog == null)
            throw new ServiceException("Unable to acquire catalog resource for layer: "
                    + layer.getName());

        CoverageStoreInfo csinfo = cvInfo.getStore();
        if (csinfo == null)
            throw new ServiceException("Unable to acquire coverage store resource for layer: "
                    + layer.getName());

        try {
            reader = (GridCoverage2DReader) cvInfo.getGridCoverageReader(null, null);
        } catch (Throwable t) {
                 LOGGER.log(Level.SEVERE, "Unable to acquire a reader for this coverage with format: "
                                 + csinfo.getFormat().getName(), t);
        }
        if (reader == null) {
            throw new ServiceException("Unable to acquire a reader for this coverage with format: "
                    + csinfo.getFormat().getName());
        }
        ReaderDimensionsAccessor dimensions = new ReaderDimensionsAccessor(reader);
        
        // Process only custom dimensions supported by the reader
        if (hasCustomDimensions) {
            for (String key : customDimensions.keySet()) {
                if (!dimensions.hasDomain(key)) customDimensions.remove(key);
            }
        }

        if (hasElevation) {
            hasDimensionsInfo(elevInfo, hasTime ? timeInfo : null);
        }

        // timeDimension
        if (hasTime && dimensions.hasTime()) {
            handleTimeDimensionRaster(cvInfo, timeInfo, dimensions);
        }

        // elevationDomain
        if (hasElevation && dimensions.hasElevation()) {
            handleElevationDimensionRaster(cvInfo, elevInfo, dimensions);
        }
        
        // custom dimensions
        if (hasCustomDimensions) {
            for (String key : customDimensions.keySet()) {
                DimensionInfo dimensionInfo = customDimensions.get(key);
                handleCustomDimensionRaster(cvInfo, key, dimensionInfo, dimensions);
            }
        }
    }

    private void handleElevationDimensionRaster(CoverageInfo cvInfo, DimensionInfo elevInfo, ReaderDimensionsAccessor dimensions) throws IOException {
        TreeSet<Object> elevations = dimensions.getElevationDomain();
        String elevationMetadata = getZDomainRepresentation(elevInfo, elevations);
        String defaultValue = getDefaultValueRepresentation(cvInfo, ResourceInfo.ELEVATION, "0");
        handleElevationDimension(elevations, elevationMetadata,
                elevInfo.getUnits(), elevInfo.getUnitSymbol(), defaultValue);
    }

    private String getDefaultValueRepresentation(ResourceInfo resource, String dimensionName, String fallback) {
        DimensionInfo dimensionInfo = wms.getDimensionInfo(resource, dimensionName);
        DimensionDefaultValueSelectionStrategy strategy = wms.getDefaultValueStrategy(resource, dimensionName, dimensionInfo);
        String defaultValue = strategy.getCapabilitiesRepresentation(resource, dimensionName, dimensionInfo);
        if(defaultValue == null) {
            defaultValue = fallback;
        } 
        return defaultValue;
    }

    private void handleTimeDimensionRaster(CoverageInfo cvInfo, DimensionInfo timeInfo, ReaderDimensionsAccessor dimension) throws IOException {
        TreeSet<Object> temporalDomain = dimension.getTimeDomain();
        String timeMetadata = getTemporalDomainRepresentation(timeInfo, temporalDomain);
        String defaultValue = getDefaultValueRepresentation(cvInfo, ResourceInfo.TIME, DimensionDefaultValueSetting.TIME_CURRENT);
        handleTimeDimension(timeMetadata, defaultValue);
    }
    
    private void handleCustomDimensionRaster(CoverageInfo cvInfo, String dimName, DimensionInfo dimension,
            ReaderDimensionsAccessor dimAccessor) throws IOException {
        final List<String> values = dimAccessor.getDomain(dimName);
        String metadata = getCustomDomainRepresentation(dimName, dimension, values);
        String defaultValue = wms.getDefaultCustomDimensionValue(dimName, cvInfo, String.class);
        handleCustomDimension(dimName, metadata, defaultValue, dimension.getUnits(), dimension.getUnitSymbol());
    }

    /**
     * Builds a single time range from the domain, be it made of Date or TimeRange objects
     * @param values
     *
     */
    protected DateRange getMinMaxTimeInterval(TreeSet<?> values) {
        Object minValue = values.first();
        Object maxValue = values.last();
        Date min, max;
        if(minValue instanceof DateRange) {
            min = ((DateRange) minValue).getMinValue();
        } else {
            min = (Date) minValue; 
        }
        if(maxValue instanceof DateRange) {
            max = ((DateRange) maxValue).getMaxValue();
        } else {
            max = (Date) maxValue; 
        }
        return new DateRange(min, max);
    }
    
    /**
     * Builds a single Z range from the domain, be it made of Double or NumberRange objects
     * @param values
     *
     */
    @SuppressWarnings("unchecked")
    protected NumberRange<Double> getMinMaxZInterval(TreeSet<?> values) {
        Object minValue = values.first();
        Object maxValue = values.last();
        Double min, max;
        if(minValue instanceof NumberRange) {
            min = ((NumberRange<Double>) minValue).getMinValue();
        } else {
            min = (Double) minValue; 
        }
        if(maxValue instanceof NumberRange) {
            max = ((NumberRange<Double>) maxValue).getMaxValue();
        } else {
            max = (Double) maxValue; 
        }
        return new NumberRange<>(Double.class, min, max);
    }


    /**
     * Returns true if all the values in the set are Date instances
     * 
     * @param values
     *
     */
    protected boolean allDates(TreeSet<?> values) {
        for(Object value : values) {
            if(!(value instanceof Date)) {
               return false; 
            }
        }
        
        return true;
    }
    
    /**
     * Returns true if all the values in the set are Double instances
     * 
     * @param values
     *
     */
    protected boolean allDoubles(TreeSet<?> values) {
        for(Object value : values) {
            if(!(value instanceof Double)) {
               return false; 
            }
        }
        
        return true;
    }

    /**
     * Writes out metadata for the time dimension
     * 
     * @param typeInfo
     * @throws IOException
     */
    private void handleTimeDimensionVector(FeatureTypeInfo typeInfo) throws IOException {
        // build the time dim representation
        TreeSet<Date> values = wms.getFeatureTypeTimes(typeInfo);
        String timeMetadata;
        if (values != null && !values.isEmpty()) {
            DimensionInfo timeInfo = typeInfo.getMetadata().get(ResourceInfo.TIME,
                DimensionInfo.class);
            timeMetadata = getTemporalDomainRepresentation(timeInfo, values);
        } else {
            timeMetadata = "";
        }
        String defaultValue = getDefaultValueRepresentation(typeInfo, ResourceInfo.TIME, DimensionDefaultValueSetting.TIME_CURRENT);
        handleTimeDimension(timeMetadata, defaultValue);
    }
    
    private void handleElevationDimensionVector(FeatureTypeInfo typeInfo) throws IOException {
        TreeSet<Double> elevations = wms.getFeatureTypeElevations(typeInfo);
        String elevationMetadata;
        DimensionInfo di = typeInfo.getMetadata().get(ResourceInfo.ELEVATION,
                DimensionInfo.class);
        String units = di.getUnits();
        String unitSymbol = di.getUnitSymbol();
        if (elevations != null && !elevations.isEmpty()) {
            elevationMetadata = getZDomainRepresentation(di, elevations);
        } else {
            elevationMetadata = "";
        }
        String defaultValue = getDefaultValueRepresentation(typeInfo, ResourceInfo.ELEVATION, "0");
        handleElevationDimension(elevations, elevationMetadata, units, unitSymbol, defaultValue);
    }
}
