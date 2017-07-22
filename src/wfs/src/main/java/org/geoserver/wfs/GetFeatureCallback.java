/* (c) 2017 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs;

import org.geoserver.platform.ExtensionPriority;

public interface GetFeatureCallback extends ExtensionPriority {

    default GetFeatureContext beforeQuerying(GetFeatureContext context) {
        // by default nothing is done
        return context;
    }

    @Override
    default int getPriority() {
        return ExtensionPriority.LOWEST;
    }
}
