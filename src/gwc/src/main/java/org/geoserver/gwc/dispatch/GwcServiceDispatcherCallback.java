/* (c) 2014 Open Source Geospatial Foundation - all rights reserved
 * (c) 2001 - 2013 OpenPlans
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.gwc.dispatch;

import java.util.HashMap;
import java.util.Map;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.gwc.layer.CatalogConfiguration;
import org.geoserver.ows.AbstractDispatcherCallback;
import org.geoserver.ows.Dispatcher;
import org.geoserver.ows.DispatcherCallback;
import org.geoserver.ows.LocalWorkspace;
import org.geoserver.ows.Request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Adapts plain incoming requests to be resolved to the GWC proxy service.
 * <p>
 * The GeoServer {@link Dispatcher} will call {@link #init(Request)} as the first step before
 * processing the request. This callback will set the {@link Request}'s service, version, and
 * request properties to the "fake" gwc service (service=gwc, version=1.0.0, request=dispatch), so
 * that when the {@link Dispatcher} looks up for the actual service bean to process the request it
 * finds out the {@link GwcServiceProxy} instance that's configured to handle such a service
 * request.
 * <p>
 * See the package documentation for more insights on how these all fit together.
 * 
 */
public class GwcServiceDispatcherCallback extends AbstractDispatcherCallback implements
        DispatcherCallback {

    private final Catalog catalog;

    public GwcServiceDispatcherCallback(Catalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public Request init(Request request) {
        String context = request.getContext();
        if (context == null || !context.contains("gwc/service")) {
            return null;
        }

        Map<String, String> kvp = new HashMap<String, String>();
        kvp.put("service", "gwc");
        kvp.put("version", "1.0.0");
        kvp.put("request", "dispatch");

        // if we are in the presence of virtual service we need to adapt the request
        WorkspaceInfo localWorkspace = LocalWorkspace.get();
        if (localWorkspace != null) {
            // this is a virtual service request
            String layerName = (String) request.getKvp().get("LAYER");
            if (layerName == null) {
                layerName = (String) request.getKvp().get("layer");
            }
            if (layerName != null) {
                // we have a layer name as parameter we eed to adapt it (gwc doesn't care about workspaces)
                layerName = CatalogConfiguration.removeWorkspacePrefix(layerName, catalog);
                layerName = localWorkspace.getName() + ":" + layerName;
                // we set the layer parameter with GWC expected name
                kvp.put("LAYER", layerName);
            }
            // we need to setup a proper context path (gwc doesn't expect the workspace to be part of the URL)
            request.setHttpRequest(new VirtualServiceRequest(request.getHttpRequest(), localWorkspace.getName(), layerName));
        }

        request.setKvp(kvp);
        request.setRawKvp(kvp);

        return request;
    }

    private final class VirtualServiceRequest extends HttpServletRequestWrapper {

        private final String localWorkspaceName;
        private final String layerName;

        private final Map<String, String[]> parameters;

        public VirtualServiceRequest(HttpServletRequest request, String localWorkspaceName, String layerName) {
            super(request);
            this.localWorkspaceName = localWorkspaceName;
            this.layerName = layerName;
            parameters = new HashMap<>(request.getParameterMap());
            if (layerName != null) {
                parameters.put("layer", new String[]{layerName});
            }
        }

        @Override
        public String getContextPath() {
            // to GWC the workspace is part of the request context
            return super.getContextPath() + "/" + localWorkspaceName;
        }

        @Override
        public String getParameter(String name) {
            if (layerName != null && name.equalsIgnoreCase("layer")) {
                return layerName;
            }
            return super.getParameter(name);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return parameters;
        }

        @Override
        public String[] getParameterValues(String name) {
            if (layerName != null && name.equalsIgnoreCase("layer")) {
                return new String[]{layerName};
            }
            return super.getParameterValues(name);
        }
    }
}
