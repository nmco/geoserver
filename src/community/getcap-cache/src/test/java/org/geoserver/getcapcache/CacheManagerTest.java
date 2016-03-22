/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.getcapcache;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public final class CacheManagerTest {

    @Test
    public void testCacheManagerOperations() throws IOException {
        // creating requests and values for the test
        RequestInfo requestA = new RequestInfoBuilder().withRequest("ServiceA").withVersion("Version1.0").build();
        CacheResult resultA = new CacheResult(requestA, "application/xml", TestUtils.stringToByteArray("REQUEST_A"));
        RequestInfo requestB = new RequestInfoBuilder().withRequest("ServiceA").withVersion("Version1.5").build();
        CacheResult resultB = new CacheResult(requestB, "application/xml", TestUtils.stringToByteArray("REQUEST_B"));
        RequestInfo requestC = new RequestInfoBuilder().withRequest("ServiceB").withVersion("Version1.0").build();
        CacheResult resultC = new CacheResult(requestC, "application/xml", TestUtils.stringToByteArray("REQUEST_C"));
        CacheManager cacheManager = new CacheManager();
        // inserting and test values for the first request
        cacheManager.put(requestA, resultA);
        assertThat(cacheManager.cache.size(), is(1));
        assertThat(cacheManager.get(requestA), notNullValue());
        assertThat(cacheManager.get(requestA).getOutput().toString(), is("REQUEST_A"));
        // inserting the first request values again (idempotent test)
        cacheManager.put(requestA, resultA);
        assertThat(cacheManager.cache.size(), is(1));
        assertThat(cacheManager.get(requestA), notNullValue());
        assertThat(cacheManager.get(requestA).getOutput().toString(), is("REQUEST_A"));
        // inserting and test values for the second request
        cacheManager.put(requestB, resultB);
        assertThat(cacheManager.cache.size(), is(2));
        assertThat(cacheManager.get(requestB), notNullValue());
        assertThat(cacheManager.get(requestB).getOutput().toString(), is("REQUEST_B"));
        // inserting and test values for the third request
        cacheManager.put(requestC, resultC);
        assertThat(cacheManager.cache.size(), is(3));
        assertThat(cacheManager.get(requestC), notNullValue());
        assertThat(cacheManager.get(requestC).getOutput().toString(), is("REQUEST_C"));
        // truncate cache
        cacheManager.truncate();
        assertThat(cacheManager.cache.size(), is(0));
    }
}
