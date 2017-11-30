/* (c) 2017 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.catalog;

/**
 * Represents special capabilities that may not be supported by all catalog implementations.
 * Normally this capabilities should be defined at the catalog facade level but each catalog
 * implementation is free to provide is own set of capabilities.
 */
public interface CatalogCapabilities {

    // default capabilities that should be assumed for catalog implementations
    CatalogCapabilities DEFAULT = () -> {
        // by default isolated workspaces are not supported
        return false;
    };

    /**
     * If this method returns TRUE it means that isolated workspaces are supported.
     *
     * @return TRUE or FALSE depending on if isolated workspaces are supported or not
     */
    boolean areIsolatedWorkspacesSupported();
}
