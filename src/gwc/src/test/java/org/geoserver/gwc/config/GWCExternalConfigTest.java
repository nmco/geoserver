/* (c) 2017 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.gwc.config;

import org.geoserver.test.GeoServerSystemTestSupport;
import org.geoserver.util.IOUtils;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests that is possible to set a different GWC configuration directory
 * using property GEOWEBCACHE_CACHE_DIR_PROPERTY.
 */
public final class GWCExternalConfigTest extends GeoServerSystemTestSupport {

    private static final File tempDirectory;

    static {
        try {
            tempDirectory = IOUtils.createTempDirectory("gwc");
        } catch (Exception exception) {
            throw new RuntimeException("Error initializing temporary directory.", exception);
        }
    }

    @Override
    protected void setUpSpring(List<String> springContextLocations) {
        super.setUpSpring(springContextLocations);
        // set an explicit location for gwc configuration directory before spring initialization
        System.setProperty(GeoserverXMLResourceProvider.GEOWEBCACHE_CACHE_DIR_PROPERTY, tempDirectory.getAbsolutePath());
    }

    @Test
    public void testThatExternalDirectoryIsUsed() throws Exception {
        applicationContext.getBeansOfType(GeoserverXMLResourceProvider.class)
                .values().forEach(bean -> {
            try {
                // check that configuration files are located in our custom directory
                assertThat(bean.getConfigDirectory(), notNullValue());
                assertThat(bean.getConfigDirectory().dir(), is(tempDirectory));
                assertThat(bean.getLocation(), is(new File(tempDirectory, bean.getConfigFileName()).getAbsolutePath()));
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    @AfterClass
    public static void cleanUp() throws Exception {
        // remove the temporary directory we created
        IOUtils.delete(tempDirectory);
    }
}