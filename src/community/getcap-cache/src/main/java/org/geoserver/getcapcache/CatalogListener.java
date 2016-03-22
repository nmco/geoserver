/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.getcapcache;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogException;
import org.geoserver.catalog.event.CatalogAddEvent;
import org.geoserver.catalog.event.CatalogModifyEvent;
import org.geoserver.catalog.event.CatalogPostModifyEvent;
import org.geoserver.catalog.event.CatalogRemoveEvent;
import org.geotools.util.logging.Logging;

import java.util.logging.Logger;

/**
 * <p>
 * Catalog listener that will truncate the cache on any event. Typically only a single instance of this
 * class should exists (singleton). Instances of this class are typically tied with the proper cache
 * manager in the spring context file.
 * </p>
 */
public class CatalogListener implements org.geoserver.catalog.event.CatalogListener {

    private final static Logger LOGGER = Logging.getLogger(CatalogListener.class);

    private final CacheManager cacheManager;

    public CatalogListener(Catalog catalog, CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        catalog.addListener(this);
    }

    @Override
    public void handleAddEvent(CatalogAddEvent event) throws CatalogException {
        Utils.debug(LOGGER, "Catalog add event received, truncating cache.");
        cacheManager.truncate();
    }

    @Override
    public void handleRemoveEvent(CatalogRemoveEvent event) throws CatalogException {
        Utils.debug(LOGGER, "Catalog remove event received, truncating cache.");
        cacheManager.truncate();
    }

    @Override
    public void handleModifyEvent(CatalogModifyEvent event) throws CatalogException {
        Utils.debug(LOGGER, "Catalog modify event received, truncating cache.");
        cacheManager.truncate();
    }

    @Override
    public void handlePostModifyEvent(CatalogPostModifyEvent event) throws CatalogException {
        Utils.debug(LOGGER, "Catalog post modify event received, truncating cache.");
        cacheManager.truncate();
    }

    @Override
    public void reloaded() {
        Utils.debug(LOGGER, "Catalog reloading event received, truncating cache.");
        cacheManager.truncate();
    }
}