/* (c) 2017 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.nsg;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.util.IOUtils;

import java.io.InputStream;

import static org.geoserver.nsg.versioning.TimeVersioning.disable;
import static org.geoserver.nsg.versioning.TimeVersioning.enable;

public final class TestsUtils {

    private TestsUtils() {
    }

    public static String readResource(String resourceName) {
        try (InputStream input = TestsUtils.class.getResourceAsStream(resourceName)) {
            return IOUtils.toString(input);
        } catch (Exception exception) {
            throw new RuntimeException(String.format("Error reading resource '%s'.", resourceName));
        }
    }

    public static void updateFeatureTypeTimeVersioning(Catalog catalog, String featureTypeName,
                                                       boolean enabled, String idProperty, String timeProperty) {
        FeatureTypeInfo featureType = catalog.getFeatureTypeByName(featureTypeName);
        if (enabled) {
            enable(featureType, idProperty, timeProperty);
        } else {
            disable(featureType);
        }
        catalog.save(featureType);
    }
}
