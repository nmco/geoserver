/* (c) 2017 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.nsg.versioning;

import org.geoserver.catalog.FeatureTypeInfo;

public final class TimeVersioning {

    public static final String ENABLED_KEY = "TIME_VERSIONING_ENABLED";
    public static final String NAME_PROPERTY_KEY = "TIME_VERSIONING_NAME_PROPERTY";
    public static final String TIME_PROPERTY_KEY = "TIME_VERSIONING_TIME_PROPERTY";

    public static void enable(FeatureTypeInfo featureTypeInfo, String nameProperty, String timeProperty) {
        featureTypeInfo.putParameter(ENABLED_KEY, true);
        featureTypeInfo.putParameter(NAME_PROPERTY_KEY, nameProperty);
        featureTypeInfo.putParameter(TIME_PROPERTY_KEY, timeProperty);
    }

    public static void disable(FeatureTypeInfo featureTypeInfo) {
        featureTypeInfo.putParameter(ENABLED_KEY, false);
        featureTypeInfo.putParameter(NAME_PROPERTY_KEY, null);
        featureTypeInfo.putParameter(TIME_PROPERTY_KEY, null);
    }

    public static boolean isEnabled(FeatureTypeInfo featureTypeInfo) {
        return featureTypeInfo.getParameter(ENABLED_KEY, Boolean.class, false);
    }

    public static String getNamePropertyName(FeatureTypeInfo featureTypeInfo) {
        String namePropertyName = featureTypeInfo.getParameter(NAME_PROPERTY_KEY, String.class, null);
        if (namePropertyName == null) {
            throw new RuntimeException("No name property name was provided.");
        }
        return namePropertyName;
    }

    public static String getTimePropertyName(FeatureTypeInfo featureTypeInfo) {
        String timePropertyName = featureTypeInfo.getParameter(TIME_PROPERTY_KEY, String.class, null);
        if (timePropertyName == null) {
            throw new RuntimeException("No time property name was provided.");
        }
        return timePropertyName;
    }

    public static void setEnable(FeatureTypeInfo featureTypeInfo, boolean enable) {
        featureTypeInfo.putParameter(ENABLED_KEY, enable);
    }

    public static void setIdAttribute(FeatureTypeInfo featureTypeInfo, String idAttributeName) {
        featureTypeInfo.putParameter(NAME_PROPERTY_KEY, idAttributeName);
    }

    public static void setTimeAttribute(FeatureTypeInfo featureTypeInfo, String timeAttributeName) {
        featureTypeInfo.putParameter(TIME_PROPERTY_KEY, timeAttributeName);
    }
}
