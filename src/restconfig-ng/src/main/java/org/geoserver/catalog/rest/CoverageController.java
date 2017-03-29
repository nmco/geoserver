/* (c) 2017 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.catalog.rest;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogBuilder;
import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.CoverageStoreInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.rest.RestException;
import org.geoserver.rest.wrapper.RestWrapper;
import org.geotools.util.logging.Logging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.logging.Logger;

@RestController
@ControllerAdvice
@RequestMapping(path = "/restng/workspaces/{workspace}/coverages")
public class CoverageController extends CatalogController {

    private static final Logger LOGGER = Logging.getLogger(CoverageController.class);

    @Autowired
    public CoverageController(Catalog catalog) {
        super(catalog);
    }

    @GetMapping(produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE,
            MediaType.TEXT_HTML_VALUE,
            TEXT_JSON})
    public RestWrapper<CoverageInfo> getWorkspaceCoverages(@PathVariable(name = "workspace") String workspaceName) {
        // get the workspace name space
        NamespaceInfo nameSpace = catalog.getNamespaceByPrefix(workspaceName);
        if (nameSpace == null) {
            // could not find the namespace associated with the desired workspace
            throw new RestException(String.format(
                    "Name space not found for workspace '%s'.", workspaceName), HttpStatus.NOT_FOUND);
        }
        // get all the coverages of the workspace \ name space
        List<CoverageInfo> coverages = catalog.getCoveragesByNamespace(nameSpace);
        return wrapList(coverages, CoverageInfo.class);
    }

    @GetMapping(path = "{coverage}", produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE,
            MediaType.TEXT_HTML_VALUE,
            TEXT_JSON})
    public RestWrapper<CoverageInfo> getCoverage(@PathVariable(name = "workspace") String workspaceName,
                                                 @PathVariable(name = "coverage") String coverageName) {
        // get the workspace name space
        NamespaceInfo nameSpace = catalog.getNamespaceByPrefix(workspaceName);
        if (nameSpace == null) {
            // could not find the namespace associated with the desired workspace
            throw new RestException(String.format(
                    "Name space not found for workspace '%s'.", workspaceName), HttpStatus.NOT_FOUND);
        }
        CoverageInfo coverage = catalog.getCoverageByName(nameSpace, coverageName);
        return wrapObject(coverage, CoverageInfo.class);
    }

    @PostMapping(path = "{coverage}", produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE,
            MediaType.TEXT_HTML_VALUE})
    protected void postCoverage(@RequestBody CoverageInfo coverage,
                                  @PathVariable(name = "workspace") String workspaceName) throws Exception {
        handleObjectPost(coverage, workspaceName, null);
    }

    /**
     * Helper method that handles the POST of a coverage.
     */
    protected String handleObjectPost(CoverageInfo coverage, String workspace, String coverageStoreName) throws Exception {
        if (coverage.getStore() == null) {
            CoverageStoreInfo ds = catalog.getCoverageStoreByName(workspace, coverageStoreName);
            coverage.setStore(ds);
        }
        final boolean isNew = isNewCoverage(coverage);
        String nativeCoverageName = coverage.getNativeCoverageName();
        if (nativeCoverageName == null) {
            nativeCoverageName = coverage.getNativeName();
        }
        CatalogBuilder builder = new CatalogBuilder(catalog);
        CoverageStoreInfo store = coverage.getStore();
        builder.setStore(store);

        // We handle 2 different cases here
        if (!isNew) {
            // Configuring a partially defined coverage
            builder.initCoverage(coverage, nativeCoverageName);
        } else {
            // Configuring a brand new coverage (only name has been specified)
            String specifiedName = coverage.getName();
            coverage = builder.buildCoverageByName(nativeCoverageName, specifiedName);
        }

        NamespaceInfo ns = coverage.getNamespace();
        if (ns != null && !ns.getPrefix().equals(workspace)) {
            //TODO: change this once the two can be different and we untie namespace
            // from workspace
            LOGGER.warning("Namespace: " + ns.getPrefix() + " does not match workspace: " + workspace + ", overriding.");
            ns = null;
        }

        if (ns == null) {
            //infer from workspace
            ns = catalog.getNamespaceByPrefix(workspace);
            coverage.setNamespace(ns);
        }

        coverage.setEnabled(true);
        catalog.validate(coverage, true).throwIfInvalid();
        catalog.add(coverage);

        //create a layer for the coverage
        catalog.add(builder.buildLayer(coverage));

        LOGGER.info("POST coverage " + coverageStoreName + "," + coverage.getName());
        return coverage.getName();
    }

    /**
     * This method returns {@code true} in case we have POSTed a Coverage object with the name only, as an instance
     * when configuring a new coverage which has just been harvested.
     *
     * @param coverage
     */
    private boolean isNewCoverage(CoverageInfo coverage) {
        return coverage.getName() != null && (coverage.isAdvertised()) && (!coverage.isEnabled())
                && (coverage.getAlias() == null) && (coverage.getCRS() == null)
                && (coverage.getDefaultInterpolationMethod() == null)
                && (coverage.getDescription() == null) && (coverage.getDimensions() == null)
                && (coverage.getGrid() == null) && (coverage.getInterpolationMethods() == null)
                && (coverage.getKeywords() == null) && (coverage.getLatLonBoundingBox() == null)
                && (coverage.getMetadata() == null) && (coverage.getNativeBoundingBox() == null)
                && (coverage.getNativeCRS() == null) && (coverage.getNativeFormat() == null)
                && (coverage.getProjectionPolicy() == null) && (coverage.getSRS() == null)
                && (coverage.getResponseSRS() == null) && (coverage.getRequestSRS() == null);
    }
}
