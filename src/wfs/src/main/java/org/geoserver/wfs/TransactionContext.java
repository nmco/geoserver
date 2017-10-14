/* (c) 2017 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs;

import org.geoserver.wfs.request.TransactionElement;
import org.geoserver.wfs.request.TransactionRequest;
import org.geoserver.wfs.request.TransactionResponse;

import java.util.Map;

public final class TransactionContext {

    private final TransactionElement element;
    private final TransactionRequest request;
    private final Map<?, ?> featureStores;
    private final TransactionResponse response;
    private final TransactionElementHandler handler;

    TransactionContext(TransactionElement element, TransactionRequest request, Map<?, ?> featureStores,
                       TransactionResponse response, TransactionElementHandler handler) {
        this.element = element;
        this.request = request;
        this.featureStores = featureStores;
        this.response = response;
        this.handler = handler;
    }

    public TransactionElement getElement() {
        return element;
    }

    public TransactionRequest getRequest() {
        return request;
    }

    public Map<?, ?> getFeatureStores() {
        return featureStores;
    }

    public TransactionResponse getResponse() {
        return response;
    }

    public TransactionElementHandler getHandler() {
        return handler;
    }
}