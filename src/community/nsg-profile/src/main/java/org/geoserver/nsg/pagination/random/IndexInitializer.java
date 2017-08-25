/* (c) 2017 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.nsg.pagination.random;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoserver.config.GeoServer;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.config.GeoServerInitializer;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geoserver.platform.resource.FileSystemResourceStore;
import org.geoserver.platform.resource.Resource;
import org.geoserver.platform.resource.ResourceListener;
import org.geoserver.platform.resource.ResourceNotification;
import org.geoserver.platform.resource.ResourceNotification.Kind;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.util.logging.Logging;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

/**
 * 
 * Class used to parse the configuration properties stored in <b>nsg-profile</b> module folder:
 * <ul>
 * <li><b>resultSets.storage.path</b> path where to store the serialized GetFeatureRequest with name
 * of random UUID.
 * <li><b>resultSets.timeToLive</b> time to live value, all the stored requests that have not been
 * used for a period of time bigger than this will be deleted.
 * <li><b>resultSets.db.{@link JDBCDataStoreFactory#DBTYPE}</b>
 * <li><b>resultSets.db.{@link JDBCDataStoreFactory#DATABASE}</b>
 * <li><b>resultSets.db.{@link JDBCDataStoreFactory#HOST}</b>
 * <li><b>resultSets.db.{@link JDBCDataStoreFactory#PORT}</b>
 * <li><b>resultSets.db.{@link JDBCDataStoreFactory#SCHEMA}</b>
 * <li><b>resultSets.db.{@link JDBCDataStoreFactory#USER}</b>
 * <li><b>resultSets.db.{@link JDBCDataStoreFactory#PASSWD}</b>
 * </ul>
 * All configuration properties is changeable at runtime so when this properties is updated the
 * module take the appropriate action:
 * <ul>
 * <li>When the index DB is changed the new DB should be used and the content of the old table moved
 * to the new table. If the new DB already has the index table it should be emptied,
 * <li>When the storage path is changed, the new storage path should be used and the old storage
 * path content should be moved to the new one,
 * <li>When the the time to live is changed the {@link #clean()} procedure will update.
 * </ul>
 *
 * The class is also responsible to {@link #clean()} the stored requests (result sets) that have not
 * been used for a period of time bigger than the configured time to live value
 * <p>
 * 
 * @author sandr
 *
 */

public class IndexInitializer implements GeoServerInitializer {

    static Logger LOGGER = Logging.getLogger(IndexInitializer.class);

    static final String PROPERTY_DB_PREFIX = "resultSets.db.";

    static final String PROPERTY_FILENAME = "configuration.properties";

    static final String MODULE_DIR = "nsg-profile";

    public static final String STORE_SCHEMA_NAME = "RESULT_SET";

    public static final String STORE_SCHEMA = "ID:\"\",created:0,updated:0";

    /*
     * Lock to synchronize activity of clean task with listener that changes the DB and file
     * resources
     */
    private final Object lock = new Object();

    @Override
    public void initialize(GeoServer geoServer) throws Exception {
        GeoServerResourceLoader loader = GeoServerExtensions.bean(GeoServerResourceLoader.class);
        GeoServerDataDirectory dd = new GeoServerDataDirectory(loader);
        Resource resource = dd.get(MODULE_DIR + "/" + PROPERTY_FILENAME);
        if (loader != null) {
            File directory = loader.findOrCreateDirectory(MODULE_DIR);
            File file = new File(directory, PROPERTY_FILENAME);
            // Create default configuration file
            if (!file.exists()) {
                InputStream stream = IndexInitializer.class
                        .getResourceAsStream("/" + PROPERTY_FILENAME);
                Properties properties = new Properties();
                properties.load(stream);
                // Replace GEOSERVER_DATA_DIR placeholder
                properties.replaceAll((k, v) -> ((String) v).replace("${GEOSERVER_DATA_DIR}",
                        dd.root().getPath()));
                // Create resource and save properties
                OutputStream out = resource.out();
                properties.store(out, null);
                out.close();
            }
            loadConfigurations(resource);
            // Listen for changes in configuration file and reload properties
            resource.addListener(new ResourceListener() {
                @Override
                public void changed(ResourceNotification notify) {
                    if (notify.getKind() == Kind.ENTRY_MODIFY) {
                        try {
                            loadConfigurations(resource);
                        } catch (Exception exception) {
                            throw new RuntimeException("Error reload confiugrations.", exception);
                        }
                    }
                }
            });
        }
    }

    /**
     * Helper method that
     */
    private void loadConfigurations(Resource resource) throws IOException {
        synchronized (lock) {
            Properties properties = new Properties();
            properties.load(resource.in());
            // Reload database
            Map<String, Object> params = new HashMap<>();
            params.put(JDBCDataStoreFactory.DBTYPE.key,
                    properties.get(PROPERTY_DB_PREFIX + JDBCDataStoreFactory.DBTYPE.key));
            params.put(JDBCDataStoreFactory.DATABASE.key,
                    properties.get(PROPERTY_DB_PREFIX + JDBCDataStoreFactory.DATABASE.key));
            params.put(JDBCDataStoreFactory.HOST.key,
                    properties.get(PROPERTY_DB_PREFIX + JDBCDataStoreFactory.HOST.key));
            params.put(JDBCDataStoreFactory.PORT.key,
                    properties.get(PROPERTY_DB_PREFIX + JDBCDataStoreFactory.PORT.key));
            params.put(JDBCDataStoreFactory.SCHEMA.key,
                    properties.get(PROPERTY_DB_PREFIX + JDBCDataStoreFactory.SCHEMA.key));
            params.put(JDBCDataStoreFactory.USER.key,
                    properties.get(PROPERTY_DB_PREFIX + JDBCDataStoreFactory.USER.key));
            params.put(JDBCDataStoreFactory.PASSWD.key,
                    properties.get(PROPERTY_DB_PREFIX + JDBCDataStoreFactory.PASSWD.key));
            /**
             * When the index DB is changed the new DB should be used and the content of the old
             * table moved to the new table. If the new DB already has the index table it should be
             * emptied
             */
            manageDBChange(params);
            /*
             * If the storage path is changed, the new storage path should be used and the old
             * storage path content should be moved to the new one
             */
            manageStorageChange(resource, properties.get("resultSets.storage.path"));
            /*
             * Change time to live
             */
            manageTimeToLiveChange(properties.get("resultSets.timeToLive"));
        }

    }

    /**
     * Helper method that
     */
    private void manageTimeToLiveChange(Object timneToLive) {
        try {
            if (timneToLive != null) {
                String timneToLiveStr = (String) timneToLive;
                IndexConfiguration.setTimeToLive(Long.parseLong(timneToLiveStr));
            }
        } catch (Exception exception) {
            throw new RuntimeException("Error on change time to live", exception);
        }
    }

    /**
     * Helper method that move resources files form current folder to the new one, current storage
     * is deleted
     */
    private void manageStorageChange(Resource resource, Object newStorage) {
        try {
            if (newStorage != null) {
                String newStorageStr = (String) newStorage;
                Resource newResource = new FileSystemResourceStore(new File(newStorageStr)).get("");
                Resource exResource = IndexConfiguration.getStorageResource();
                if (exResource != null && !newResource.dir().getAbsolutePath()
                        .equals(exResource.dir().getAbsolutePath())) {
                    exResource.delete();
                    IndexConfiguration.setStorageResource(newResource);
                }
            }
        } catch (Exception exception) {
            throw new RuntimeException("Error on change store", exception);
        }
    }

    /**
     * Helper method that move DB data from old store to new one
     */
    private void manageDBChange(Map<String, Object> params) {
        try {
            DataStore exDataStore = IndexConfiguration.getCurrentDataStore();
            DataStore newDataStore = DataStoreFinder.getDataStore(params);
            if (exDataStore != null) {
                // New store is valid and is different from current one
                if (newDataStore != null && !isStorageTheSame(params)) {
                    // Create table in new store
                    createTable(newDataStore, true);
                    // Move data to new store
                    moveData(exDataStore, newDataStore);
                    // Delete old store
                    exDataStore.dispose();
                }
            } else {
                // Create schema
                createTable(newDataStore, false);
            }
            IndexConfiguration.setCurrentDataStore(params, newDataStore);
        } catch (Exception exception) {
            throw new RuntimeException("Error reload DB confiugrations.", exception);
        }
    }

    /**
     * Helper method that check id the DB is the same, matching the JDBC configurations parameters.
     */
    private Boolean isStorageTheSame(Map<String, Object> newParams) {
        Map<String, Object> currentParams = IndexConfiguration.getCurrentDataStoreParams();
        return currentParams.get(JDBCDataStoreFactory.DBTYPE.key)
                .equals(newParams.get(JDBCDataStoreFactory.DBTYPE.key))
                && currentParams.get(JDBCDataStoreFactory.DATABASE.key)
                        .equals(newParams.get(JDBCDataStoreFactory.DATABASE.key))
                && currentParams.get(JDBCDataStoreFactory.HOST.key)
                        .equals(newParams.get(JDBCDataStoreFactory.HOST.key))
                && currentParams.get(JDBCDataStoreFactory.PORT.key)
                        .equals(newParams.get(JDBCDataStoreFactory.PORT.key))
                && currentParams.get(JDBCDataStoreFactory.SCHEMA.key)
                        .equals(newParams.get(JDBCDataStoreFactory.SCHEMA.key));
    }

    /**
     * Helper method that create a new table on DB to store resource informations
     */
    private void createTable(DataStore dataStore, Boolean forceDelete) throws Exception {
        SimpleFeatureType schema = dataStore.getSchema(STORE_SCHEMA_NAME);
        // Schema exists
        if (schema != null) {
            // Delete of exist is required, and then create a new one
            if (forceDelete) {
                dataStore.removeSchema(STORE_SCHEMA_NAME);
                schema = DataUtilities.createType(STORE_SCHEMA_NAME, STORE_SCHEMA);
                dataStore.createSchema(schema);
            }
            // Schema not exists, create a new one
        } else {
            schema = DataUtilities.createType(STORE_SCHEMA_NAME, STORE_SCHEMA);
            dataStore.createSchema(schema);
        }
    }

    /**
     * Helper method that move resource informations from current DB to the new one
     */
    private void moveData(DataStore exDataStore, DataStore newDataStore) throws Exception {
        Transaction session = new DefaultTransaction("Adding");
        try {
            SimpleFeatureSource exFs = exDataStore.getFeatureSource(STORE_SCHEMA_NAME);
            SimpleFeatureStore newFs = (SimpleFeatureStore) newDataStore
                    .getFeatureSource(STORE_SCHEMA_NAME);
            newFs.setTransaction(session);
            newFs.addFeatures(exFs.getFeatures());
            session.commit();
        } catch (Throwable t) {
            session.rollback();
            throw new RuntimeException("Error on move data", t);
        } finally {
            session.close();
        }
    }

    /**
     * Delete all the stored requests (result sets) that have not been used for a period of time
     * bigger than the configured time to live value. Clean also related resource files.
     * <p>
     * Executed by scheduler, for details see Spring XML configuration
     */
    public void clean() throws Exception {
        synchronized (lock) {
            Transaction session = new DefaultTransaction("RemoveOld");
            try {
                // Remove record
                Long timeToLive = IndexConfiguration.getTimeToLive();
                DataStore currentDataStore = IndexConfiguration.getCurrentDataStore();
                SimpleFeatureStore store = (SimpleFeatureStore) currentDataStore
                        .getFeatureSource(STORE_SCHEMA_NAME);
                Long now = new Date().getTime();
                Long liveTreshold = now - timeToLive * 1000;
                Filter filter = CQL.toFilter("updated < " + liveTreshold);
                SimpleFeatureCollection toRemoved = store.getFeatures(filter);
                // Remove file
                Resource currentResource = IndexConfiguration.getStorageResource();
                SimpleFeatureIterator iterator = toRemoved.features();
                try {
                    while (iterator.hasNext()) {
                        SimpleFeature feature = iterator.next();
                        currentResource.get(feature.getID()).delete();
                    }
                } finally {
                    iterator.close();
                }
                store.removeFeatures(filter);
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("CLEAN executed, removed stored requests older than "
                            + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                    .format(new Date(liveTreshold)));
                }
            } catch (Throwable t) {
                session.rollback();
                throw new RuntimeException("Error on move data", t);
            } finally {
                session.close();
            }
        }
    }
}
