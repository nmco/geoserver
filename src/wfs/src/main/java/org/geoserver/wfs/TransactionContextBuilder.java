/* (c) 2017 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs;

import org.geoserver.wfs.request.TransactionElement;
import org.geoserver.wfs.request.TransactionRequest;
import org.geoserver.wfs.request.TransactionResponse;

import java.util.Map;

public final class TransactionContextBuilder {

    private TransactionElement element;
    private TransactionRequest request;
    private Map<?, ?> featureStores;
    private TransactionResponse response;
    private TransactionElementHandler handler;

    public TransactionContextBuilder() {
    }

    public TransactionContextBuilder withElement(TransactionElement element) {
        this.element = element;
        return this;
    }

    public TransactionContextBuilder withRequest(TransactionRequest request) {
        this.request = request;
        return this;
    }

    public TransactionContextBuilder withFeatureStores(Map<?, ?> featureStores) {
        this.featureStores = featureStores;
        return this;
    }

    public TransactionContextBuilder withResponse(TransactionResponse response) {
        this.response = response;
        return this;
    }

    public TransactionContextBuilder withHandler(TransactionElementHandler handler) {
        this.handler = handler;
        return this;
    }

    public TransactionContextBuilder withContext(TransactionContext context) {
        this.element = context.getElement();
        this.featureStores = context.getFeatureStores();
        this.handler = context.getHandler();
        this.request = context.getRequest();
        this.response = context.getResponse();
        return this;
    }

    public TransactionContext build() {
        return new TransactionContext(element, request, featureStores, response, handler);
    }
}
