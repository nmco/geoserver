package org.geoserver.appschema.smart.data;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.geoserver.appschema.smart.data.store.ExclusionsDomainModelVisitor;
import org.geoserver.appschema.smart.domain.DomainModelBuilder;
import org.geoserver.appschema.smart.domain.DomainModelConfig;
import org.geoserver.appschema.smart.domain.entities.DomainModel;
import org.geoserver.appschema.smart.metadata.DataStoreMetadata;
import org.geoserver.appschema.smart.metadata.DataStoreMetadataConfig;
import org.geoserver.appschema.smart.metadata.DataStoreMetadataFactory;
import org.geoserver.appschema.smart.metadata.jdbc.JdbcDataStoreMetadataConfig;
import org.geoserver.appschema.smart.visitors.appschema.AppSchemaVisitor;
import org.geoserver.appschema.smart.visitors.gml.GmlSchemaVisitor;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.resource.Resource;
import org.geotools.data.DataAccess;
import org.geotools.data.DataAccessFactory;
import org.geotools.data.DataUtilities;
import org.geotools.data.Parameter;
import org.geotools.data.complex.AppSchemaDataAccess;
import org.geotools.data.complex.FeatureTypeMapping;
import org.geotools.data.complex.config.AppSchemaDataAccessConfigurator;
import org.geotools.data.complex.config.AppSchemaDataAccessDTO;
import org.geotools.data.complex.config.DataAccessMap;
import org.geotools.data.complex.config.XMLConfigDigester;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.util.URLs;
import org.geotools.util.logging.Logging;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.w3c.dom.Document;

/**
 * Smart AppSchema DataStore factory.
 *
 * @author Jose Macchi - GeoSolutions
 */
public class PostgisSmartAppSchemaDataAccessFactory implements DataAccessFactory {

    static final Logger LOGGER = Logging.getLogger(PostgisSmartAppSchemaDataAccessFactory.class);

    public static final String DBTYPE_STRING = "app-schema-smart";

    public static final Param DBTYPE =
            new Param(
                    "dbtype",
                    String.class,
                    "Fixed value '" + DBTYPE_STRING + "'",
                    true,
                    DBTYPE_STRING,
                    Collections.singletonMap(Parameter.LEVEL, "program"));

    public static final Param NAMESPACE =
            new Param("namespace", URI.class, "Namespace prefix", false);
    public static final Param DATASTORE_NAME =
            new Param("datastorename", String.class, "Name of the datastore", false);
    public static final Param ROOT_ENTITY =
            new Param("root entity", String.class, "Root Entity", true);
    public static final Param POSTGIS_DATASTORE_METADATA =
            new Param("datastore", String.class, "Postgis related DataStore", true);
    public static final Param DOMAIN_MODEL_EXCLUSIONS =
            new Param(
                    "excluded objects",
                    String.class,
                    "Excluded comma separated domainmodel object list",
                    false);

    @Override
    public String getDisplayName() {
        return "AppSchema Smart";
    }

    @Override
    public String getDescription() {
        return "AppSchema Smart builder tool";
    }

    @Override
    public final Param[] getParametersInfo() {
        LinkedHashMap map = new LinkedHashMap();
        setupParameters(map);
        return (Param[]) map.values().toArray(new Param[map.size()]);
    }

    @Override
    public boolean isAvailable() {
        try {
            Class.forName(getDriverClassName());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean canProcess(Map params) {
        try {
            Object dbType = DBTYPE.lookUp(params);
            return DBTYPE_STRING.equals(dbType)
                    && DataUtilities.canProcess(params, getParametersInfo());
        } catch (Exception e) {
            // do nothing. based on AppSchemaDataAccessFactory code
        }
        return false;
    }

    public String getDriverClassName() {
        return "org.postgresql.Driver";
    }

    public String getDatabaseID() {
        return "postgis";
    }

    private void setupParameters(Map parameters) {
        parameters.put(DBTYPE.key, DBTYPE);
        parameters.put(NAMESPACE.key, NAMESPACE);
        parameters.put(DATASTORE_NAME.key, DATASTORE_NAME);
        parameters.put(POSTGIS_DATASTORE_METADATA.key, POSTGIS_DATASTORE_METADATA);
        parameters.put(ROOT_ENTITY.key, ROOT_ENTITY);
        parameters.put(DOMAIN_MODEL_EXCLUSIONS.key, DOMAIN_MODEL_EXCLUSIONS);
    }

    private String getFilenamePrefix(Map params) throws IOException {
        String rootEntity = lookup(ROOT_ENTITY, params, String.class);
        String filenamePrefix = rootEntity;
        return filenamePrefix;
    }
    /**
     * Helper method that based on parameters, builds domainmodel, generates associated mapping
     * files and saves them in the workspace, returning the resulting DataStore.
     */
    private DataAccess<FeatureType, Feature> createDataStore(
            Map params,
            boolean hidden,
            DataAccessMap sourceDataStoreMap,
            final Set<AppSchemaDataAccess> registeredAppSchemaStores)
            throws IOException {
        // get parameters
        URI namespace = lookup(NAMESPACE, params, URI.class);
        String datastoreName = lookup(DATASTORE_NAME, params, String.class);
        String excludedObjects = lookup(DOMAIN_MODEL_EXCLUSIONS, params, String.class);

        // convert excluded objects in a list
        String[] elements = {};
        if (excludedObjects != null) {
            elements = excludedObjects.split(",");
        }
        List<String> excludedObjectsList = Arrays.asList(elements);

        // build domainmodel, and exclude elements based on excludedObjects list
        DomainModel dm = buildDomainModel(params, excludedObjectsList);
        // define filenames naming convention for documents to be saved
        String gmlFilename = getFilenamePrefix(params) + "-gml.xsd";
        String appschemaFilename = getFilenamePrefix(params) + "-appschema.xml";
        // String gmlFilename = rootEntity+"-gml.xsd";
        // String appschemaFilename = rootEntity+"-appschema.xml";
        GeoServerDataDirectory gdd =
                ((GeoServerDataDirectory) GeoServerExtensions.bean("dataDirectory"));
        String target_namespace = namespace.toASCIIString();
        Catalog c = ((GeoServer) GeoServerExtensions.bean("geoServer")).getCatalog();
        NamespaceInfo ni = c.getNamespaceByURI(target_namespace);
        WorkspaceInfo wi = c.getWorkspaceByName(ni.getName());
        String namespace_prefix = c.getNamespaceByURI(target_namespace).getPrefix();
        Resource wiFolder = gdd.get(wi, "");
        // create folder called appschema-smart inside the datastore folder
        String pathname = wiFolder.toString() + "/" + datastoreName + "/appschema-mappings/";

        // populate appschema model visitor
        AppSchemaVisitor appSchemaDmv =
                new AppSchemaVisitor(namespace_prefix, target_namespace, "./" + gmlFilename);
        dm.accept(appSchemaDmv);
        // populate gml model visitor
        GmlSchemaVisitor gmlDmv = new GmlSchemaVisitor(namespace_prefix, target_namespace);
        dm.accept(gmlDmv);

        // save datamodel related files
        File appschemaFile =
                saveMappingDocument(pathname, appschemaFilename, appSchemaDmv.getDocument());
        File gmlFile = saveMappingDocument(pathname, gmlFilename, gmlDmv.getDocument());

        // define datastore mappings and save datastore
        Set<FeatureTypeMapping> mappings;
        AppSchemaDataAccess dataStore;
        URL configFileUrl = URLs.fileToUrl(appschemaFile);
        XMLConfigDigester configReader = new XMLConfigDigester();
        AppSchemaDataAccessDTO config = configReader.parse(configFileUrl);
        List<String> includes = config.getIncludes();
        for (Iterator<String> it = includes.iterator(); it.hasNext(); ) {
            params.put("url", buildIncludeUrl(configFileUrl, it.next()));
            createDataStore(params, true, sourceDataStoreMap, registeredAppSchemaStores);
        }
        mappings = AppSchemaDataAccessConfigurator.buildMappings(config, sourceDataStoreMap);
        dataStore = new AppSchemaDataAccess(mappings, hidden);
        registeredAppSchemaStores.add(dataStore);
        return dataStore;
    }

    /** Helper method that allows to create the DomainModel. */
    private DomainModel buildDomainModel(Map params, List exclusions) throws IOException {
        DataStoreInfo jdbcDataStoreInfo = this.getDataStoreInfoByName(params);
        String rootEntity = lookup(ROOT_ENTITY, params, String.class);
        PostgisNGDataStoreFactory factory = new PostgisNGDataStoreFactory();
        JDBCDataStore jdbcDataStore = null;
        DataStoreMetadata dsm = null;
        try {
            // TODO need to review (since it's forcing to get a JDBC datastore based on parameters.
            // Not sure what happen with JNDI)
            jdbcDataStore = factory.createDataStore(jdbcDataStoreInfo.getConnectionParameters());
            DataStoreMetadataConfig config =
                    new JdbcDataStoreMetadataConfig(
                            jdbcDataStore,
                            jdbcDataStoreInfo.getConnectionParameters().get("passwd").toString());
            dsm = (new DataStoreMetadataFactory()).getDataStoreMetadata(config);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error retrieving metadata from DB.");
        }

        if (dsm == null) {
            // cannot get datastoremetadata connected from which obtain db metadata
            throw new RuntimeException("Cannot connect to DB with defined parameters.");
        }
        DomainModelConfig dmc = new DomainModelConfig();
        dmc.setRootEntityName(rootEntity);
        DomainModelBuilder dmb = new DomainModelBuilder(dsm, dmc);
        DomainModel dm = dmb.buildDomainModel();
        // apply exclusions to original model
        DomainModel newDomainModel = ExclusionsDomainModelVisitor.buildDomainModel(dm, exclusions);
        // release datastore before returning model
        jdbcDataStore.dispose();
        return newDomainModel;
    }

    /**
     * Helper method that allows to save an xml document representing a mapping in smart-appschema
     * folder.
     */
    private File saveMappingDocument(String pathname, String filename, Document mapping) {
        // create smart-appschema folder in workspace folder if it does not exists
        File directory = new File(pathname);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        // save document on filename
        File file = null;
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transf = transformerFactory.newTransformer();
            transf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transf.setOutputProperty(OutputKeys.INDENT, "yes");
            transf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(mapping);
            file = new File(pathname + filename);
            StreamResult stream = new StreamResult(file);
            transf.transform(source, stream);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "Cannot save generated mapping in the workspace related folder.");
        }
        return file;
    }

    /** Method that allows to get a DataStoreInfo based on a set of parameters. */
    private DataStoreInfo getDataStoreInfoByName(Map params) throws IOException {
        String jdbcDataStoreInfoName = lookup(POSTGIS_DATASTORE_METADATA, params, String.class);
        URI namespace = lookup(NAMESPACE, params, URI.class);
        String target_namespace = namespace.toASCIIString();
        Catalog c = ((GeoServer) GeoServerExtensions.bean("geoServer")).getCatalog();
        NamespaceInfo ni = c.getNamespaceByURI(target_namespace);
        WorkspaceInfo wi = c.getWorkspaceByName(ni.getName());
        DataStoreInfo ds = c.getDataStoreByName(wi.getName(), jdbcDataStoreInfoName);
        return ds;
    }

    /** Helper method to build urls in the context of a new AppSchemaDataAccess instance. */
    private String buildIncludeUrl(URL parentUrl, String include) {
        // first check if the include is already an URL
        String includeLowerCase = include.toLowerCase();
        if (includeLowerCase.startsWith("http:") || includeLowerCase.startsWith("file:")) {
            // we already have an URL, return it has is
            return include;
        }
        // we need to build an URL using the parent URL as a basis
        String url = parentUrl.toString();
        int index = url.lastIndexOf("/");
        if (index <= 0) {
            // we can't handle this situation let's raise an exception
            throw new RuntimeException(
                    String.format(
                            "Can't build include types '%s' URL using parent '%s' URL.",
                            include, url));
        }
        // build the include types URL
        url = url.substring(0, index + 1) + include;
        LOGGER.fine(
                String.format("Using URL '%s' to retrieve include types with '%s'.", url, include));
        return url;
    }

    /** Helper for getting values on parameters mappings. */
    <T> T lookup(Param param, Map<String, Serializable> params, Class<T> target)
            throws IOException {
        T result = target.cast(param.lookUp(params));
        if (result == null) {
            result = target.cast(param.getDefaultValue());
        }
        return result;
    }

    @Override
    public DataAccess<? extends FeatureType, ? extends Feature> createDataStore(
            Map<String, ?> params) throws IOException {
        final Set<AppSchemaDataAccess> registeredAppSchemaStores =
                new HashSet<AppSchemaDataAccess>();
        try {
            return createDataStore(params, false, new DataAccessMap(), registeredAppSchemaStores);
        } catch (Exception ex) {
            // dispose every already registered included datasource
            for (AppSchemaDataAccess appSchemaDataAccess : registeredAppSchemaStores) {
                appSchemaDataAccess.dispose();
            }
            throw ex;
        }
    }
}
