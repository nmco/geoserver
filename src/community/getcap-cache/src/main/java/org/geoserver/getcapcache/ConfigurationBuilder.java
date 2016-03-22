/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.getcapcache;

import org.apache.commons.net.util.SubnetUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Builder that helps build a valid configuration. By default the caching will be enable and
 * there is not black listed or white listed rules.
 * </p>
 */
class ConfigurationBuilder {

    private Boolean isCachingEnable;
    private List<SubnetUtils.SubnetInfo> blackListedIps = new ArrayList<SubnetUtils.SubnetInfo>();
    private List<SubnetUtils.SubnetInfo> whitedListedIps = new ArrayList<SubnetUtils.SubnetInfo>();

    ConfigurationBuilder withCaching(Boolean caching) {
        this.isCachingEnable = caching;
        return this;
    }

    ConfigurationBuilder withBlackListedIps(SubnetUtils.SubnetInfo blackListedIps) {
        Utils.checkNotNull(blackListedIps, "Black list IP rule cannot be NULL.");
        this.blackListedIps.add(blackListedIps);
        return this;
    }

    ConfigurationBuilder withWhitedListedIps(SubnetUtils.SubnetInfo whitedListedIps) {
        Utils.checkNotNull(whitedListedIps, "White list IP rule cannot be NULL.");
        this.whitedListedIps.add(whitedListedIps);
        return this;
    }

    Configuration build() {
        // by default caching is enable
        isCachingEnable = Utils.withDefault(isCachingEnable, true);
        // black listed rules and white listed rules are valid by design
        return new Configuration(isCachingEnable, blackListedIps, whitedListedIps);
    }
}