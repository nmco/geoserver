/* (c) 2017 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs;

import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.wfs.request.GetFeatureRequest;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;

public final class GetFeatureContext {

    private final GetFeatureRequest request;
    private final FeatureSource<? extends FeatureType, ? extends Feature> featureSource;
    private final Query query;
    private final FeatureTypeInfo featureTypeInfo;

    GetFeatureContext(GetFeatureRequest request, FeatureSource<? extends FeatureType, ? extends Feature> featureSource,
                             Query query, FeatureTypeInfo featureTypeInfo) {
        this.request = request;
        this.featureSource = featureSource;
        this.query = query;
        this.featureTypeInfo = featureTypeInfo;
    }

    public GetFeatureRequest getRequest() {
        return request;
    }

    public FeatureSource<? extends FeatureType, ? extends Feature> getFeatureSource() {
        return featureSource;
    }

    public Query getQuery() {
        return query;
    }

    public FeatureTypeInfo getFeatureTypeInfo() {
        return featureTypeInfo;
    }
}