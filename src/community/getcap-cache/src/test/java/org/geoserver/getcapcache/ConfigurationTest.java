/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.getcapcache;

import org.junit.Test;

import java.io.InputStream;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class ConfigurationTest {

    @Test
    public void testEmptyConfiguration() {
        new TestUtils.DoWorkWithResource("configuration1.xml") {
            @Override
            public void doWork(InputStream inputStream) {
                Configuration configuration = ConfigurationParser.parse(inputStream);
                assertThat(configuration, notNullValue());
                assertThat(configuration.isCachingEnable(), is(true));
                assertThat(configuration.getBlackListedIps(), notNullValue());
                assertThat(configuration.getBlackListedIps().size(), is(0));
                assertThat(configuration.getWhitedListedIps(), notNullValue());
                assertThat(configuration.getWhitedListedIps().size(), is(0));
                assertThat(configuration.isIpAddressCacheable("192.168.34.0"), is(true));
            }
        };
    }

    @Test
    public void testDisabledCaching() {
        new TestUtils.DoWorkWithResource("configuration2.xml") {
            @Override
            public void doWork(InputStream inputStream) {
                Configuration configuration = ConfigurationParser.parse(inputStream);
                assertThat(configuration, notNullValue());
                assertThat(configuration.isCachingEnable(), is(false));
                assertThat(configuration.getBlackListedIps(), notNullValue());
                assertThat(configuration.getBlackListedIps().size(), is(0));
                assertThat(configuration.getWhitedListedIps(), notNullValue());
                assertThat(configuration.getWhitedListedIps().size(), is(0));
                assertThat(configuration.isIpAddressCacheable("192.168.34.0"), is(true));
            }
        };
    }

    @Test
    public void testBlacklist() {
        new TestUtils.DoWorkWithResource("configuration3.xml") {
            @Override
            public void doWork(InputStream inputStream) {
                Configuration configuration = ConfigurationParser.parse(inputStream);
                assertThat(configuration, notNullValue());
                assertThat(configuration.isCachingEnable(), is(true));
                assertThat(configuration.getBlackListedIps(), notNullValue());
                assertThat(configuration.getBlackListedIps().size(), is(2));
                assertThat(configuration.getWhitedListedIps(), notNullValue());
                assertThat(configuration.getWhitedListedIps().size(), is(0));
                assertThat(configuration.isIpAddressCacheable("192.168.34.0"), is(true));
                assertThat(configuration.isIpAddressCacheable("192.168.0.2"), is(true));
                assertThat(configuration.isIpAddressCacheable("192.168.0.1"), is(false));
                assertThat(configuration.isIpAddressCacheable("192.169.33.0"), is(false));
                assertThat(configuration.isIpAddressCacheable("192.169.33.50"), is(false));
                assertThat(configuration.isIpAddressCacheable("192.169.33.255"), is(false));
                assertThat(configuration.isIpAddressCacheable("192.169.32.255"), is(true));
            }
        };
    }

    @Test
    public void testWhitelist() {
        new TestUtils.DoWorkWithResource("configuration4.xml") {
            @Override
            public void doWork(InputStream inputStream) {
                Configuration configuration = ConfigurationParser.parse(inputStream);
                assertThat(configuration, notNullValue());
                assertThat(configuration.isCachingEnable(), is(true));
                assertThat(configuration.getBlackListedIps(), notNullValue());
                assertThat(configuration.getBlackListedIps().size(), is(0));
                assertThat(configuration.getWhitedListedIps(), notNullValue());
                assertThat(configuration.getWhitedListedIps().size(), is(2));
                assertThat(configuration.isIpAddressCacheable("192.168.34.0"), is(false));
                assertThat(configuration.isIpAddressCacheable("192.170.2.44"), is(false));
                assertThat(configuration.isIpAddressCacheable("192.170.2.45"), is(true));
                assertThat(configuration.isIpAddressCacheable("192.171.10.0"), is(true));
                assertThat(configuration.isIpAddressCacheable("192.171.10.85"), is(true));
                assertThat(configuration.isIpAddressCacheable("192.171.10.255"), is(true));
                assertThat(configuration.isIpAddressCacheable("192.169.33.50"), is(false));
            }
        };
    }

    @Test
    public void testBlacklistAndWhitelist() {
        new TestUtils.DoWorkWithResource("configuration5.xml") {
            @Override
            public void doWork(InputStream inputStream) {
                Configuration configuration = ConfigurationParser.parse(inputStream);
                assertThat(configuration, notNullValue());
                assertThat(configuration.isCachingEnable(), is(true));
                assertThat(configuration.getBlackListedIps(), notNullValue());
                assertThat(configuration.getBlackListedIps().size(), is(2));
                assertThat(configuration.getWhitedListedIps(), notNullValue());
                assertThat(configuration.getWhitedListedIps().size(), is(3));
                assertThat(configuration.isIpAddressCacheable("192.90.50.5"), is(false));
                assertThat(configuration.isIpAddressCacheable("192.170.25.9"), is(false));
                assertThat(configuration.isIpAddressCacheable("192.160.15.20"), is(false));
                assertThat(configuration.isIpAddressCacheable("192.180.10.0"), is(false));
                assertThat(configuration.isIpAddressCacheable("192.180.10.35"), is(false));
                assertThat(configuration.isIpAddressCacheable("192.180.10.255"), is(false));
                assertThat(configuration.isIpAddressCacheable("192.160.15.0"), is(true));
                assertThat(configuration.isIpAddressCacheable("192.160.15.25"), is(true));
                assertThat(configuration.isIpAddressCacheable("192.160.15.255"), is(true));
                assertThat(configuration.isIpAddressCacheable("192.170.25.8"), is(true));
            }
        };
    }

    @Test
    public void testInvalidConfiguration() {
        new TestUtils.DoWorkWithResource("configuration6.xml") {
            @Override
            public void doWork(InputStream inputStream) {
                Configuration configuration = ConfigurationParser.parse(inputStream);
                assertThat(configuration, is(nullValue()));
            }
        };
    }
}
