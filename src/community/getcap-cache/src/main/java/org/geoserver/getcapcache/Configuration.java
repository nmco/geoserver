/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.getcapcache;

import org.apache.commons.net.util.SubnetUtils;

import java.util.List;

/**
 * <p>
 * This class contains all the configurations for the GetCapabilities caching.
 * Instances of this class should be created using the builder {@link ConfigurationBuilder}.
 * </p>
 */
final class Configuration {

    private final boolean isCachingEnable;

    private final List<SubnetUtils.SubnetInfo> blackListedIps;
    private final List<SubnetUtils.SubnetInfo> whitedListedIps;

    Configuration(boolean isCachingEnable, List<SubnetUtils.SubnetInfo> blackListedIps,
                  List<SubnetUtils.SubnetInfo> whitedListedIps) {
        this.isCachingEnable = isCachingEnable;
        this.blackListedIps = blackListedIps;
        this.whitedListedIps = whitedListedIps;
    }

    /**
     * Return TRUE if the caching is enabled, otherwise FALSE.
     */
    boolean isCachingEnable() {
        return isCachingEnable;
    }

    /**
     * An IP is cacheable if he is not black listed and he is white listed.
     * If there is no white listed rules all IPs are considered white listed.
     */
    boolean isIpAddressCacheable(String ipAddress) {
        // check if the ip is black listed
        for (SubnetUtils.SubnetInfo subnetInfo : blackListedIps) {
            if (subnetInfo.isInRange(ipAddress)) {
                // the ip is black listed
                return false;
            }
        }
        // the ip is not black listed
        if (whitedListedIps.isEmpty()) {
            // there is no allow ips rules, so we accept this ip
            return true;
        }
        // we checked there is an allow rule that accepts this ip
        for (SubnetUtils.SubnetInfo subnetInfo : whitedListedIps) {
            if (subnetInfo.isInRange(ipAddress)) {
                // this ip is allowed
                return true;
            }
        }
        // no rules allow this ip
        return false;
    }

    List<SubnetUtils.SubnetInfo> getBlackListedIps() {
        return blackListedIps;
    }

    List<SubnetUtils.SubnetInfo> getWhitedListedIps() {
        return whitedListedIps;
    }
}
