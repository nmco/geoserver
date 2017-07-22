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

public final class GetFeatureContextBuilder {

    private GetFeatureRequest request;
    private FeatureSource<? extends FeatureType, ? extends Feature> featureSource;
    private Query query;
    private FeatureTypeInfo featureTypeInfo;

    public GetFeatureContextBuilder() {
    }

    public GetFeatureContextBuilder withRequest(GetFeatureRequest request) {
        this.request = request;
        return this;
    }

    public GetFeatureContextBuilder withFeatureSource(FeatureSource<? extends FeatureType, ? extends Feature> featureSource) {
        this.featureSource = featureSource;
        return this;
    }

    public GetFeatureContextBuilder withQuery(Query query) {
        this.query = query;
        return this;
    }

    public GetFeatureContextBuilder withFeatureTypeInfo(FeatureTypeInfo featureTypeInfo) {
        this.featureTypeInfo = featureTypeInfo;
        return this;
    }

    public GetFeatureContextBuilder withContext(GetFeatureContext context) {
        return withRequest(context.getRequest())
                .withFeatureSource(context.getFeatureSource())
                .withQuery(context.getQuery())
                .withFeatureTypeInfo(context.getFeatureTypeInfo());
    }

    public GetFeatureContext build() {
        return new GetFeatureContext(request, featureSource, query, featureTypeInfo);
    }
}
