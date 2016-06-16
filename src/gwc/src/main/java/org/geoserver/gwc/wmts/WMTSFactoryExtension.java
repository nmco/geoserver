/* (c) 2014 Open Source Geospatial Foundation - all rights reserved
 * (c) 2001 - 2013 OpenPlans
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.gwc.wmts;

import org.geoserver.config.ServiceFactoryExtension;
import org.geoserver.gwc.wmts.WMTSInfo;
import org.geoserver.gwc.wmts.WMTSInfoImpl;

public class WMTSFactoryExtension extends ServiceFactoryExtension<WMTSInfo> {

    public WMTSFactoryExtension() {
        super(WMTSInfo.class);
    }

    @Override
    public <T> T create(Class<T> clazz) {
        return (T) new WMTSInfoImpl();
    }
}
