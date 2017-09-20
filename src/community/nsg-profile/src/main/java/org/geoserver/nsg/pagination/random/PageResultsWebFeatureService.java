/* (c) 2017 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.nsg.pagination.random;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Date;
import java.util.logging.Logger;

import org.geoserver.config.GeoServer;
import org.geoserver.ows.Dispatcher;
import org.geoserver.ows.KvpRequestReader;
import org.geoserver.ows.util.OwsUtils;
import org.geoserver.platform.resource.Resource;
import org.geoserver.wfs.DefaultWebFeatureService20;
import org.geoserver.wfs.request.FeatureCollectionResponse;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.util.logging.Logging;
import org.opengis.filter.Filter;

import net.opengis.wfs20.GetFeatureType;
import net.opengis.wfs20.ResultTypeType;

/**
 * This service supports the PageResults operation and manage it
 * 
 * @author sandr
 *
 */

public class PageResultsWebFeatureService extends DefaultWebFeatureService20 {

    static Logger LOGGER = Logging.getLogger(PageResultsWebFeatureService.class);

    private static final String GML32_FORMAT = "application/gml+xml; version=3.2";

    private static final BigInteger DEFAULT_START = new BigInteger("0");

    private static final BigInteger DEFAULT_COUNT = new BigInteger("10");

    private String resultSetId;

    public PageResultsWebFeatureService(GeoServer geoServer) {
        super(geoServer);
    }

    /**
     * 
     * Recovers the stored request with associated {@link #resultSetID} and overrides the parameters
     * using the ones provided with current operation or the default values:
     * <ul>
     * <li>{@link net.opengis.wfs20.GetFeatureType#getStartIndex <em>StartIndex</em>}</li>
     * <li>{@link net.opengis.wfs20.GetFeatureType#getCount <em>Count</em>}</li>
     * <li>{@link net.opengis.wfs20.GetFeatureType#getOutputFormat <em>OutputFormat</em>}</li>
     * <li>{@link net.opengis.wfs20.GetFeatureType#getResultType <em>ResultType</em>}</li>
     * </ul>
     * Then executes the GetFeature operation using the WFS 2.0 service implementation and return is
     * result.
     *
     * @param request
     * @return
     * @throws Exception
     */
    public FeatureCollectionResponse pageResults(GetFeatureType request) throws Exception {
        // Retrieve stored request
        GetFeatureType gft = getFeature(this.resultSetId);

        // Update with incoming parameters or defaults
        Method setBaseUrl = OwsUtils.setter(gft.getClass(), "baseUrl", String.class);
        setBaseUrl.invoke(gft, new Object[] { request.getBaseUrl() });
        BigInteger startIndex = request.getStartIndex() != null ? request.getStartIndex()
                : DEFAULT_START;
        BigInteger count = request.getCount() != null ? request.getCount() : DEFAULT_COUNT;
        String outputFormat = request.getOutputFormat() != null ? request.getOutputFormat()
                : GML32_FORMAT;
        ResultTypeType resultType = request.getResultType() != null ? request.getResultType()
                : ResultTypeType.RESULTS;
        gft.setStartIndex(startIndex);
        gft.setCount(count);
        gft.setOutputFormat(outputFormat);
        gft.setResultType(resultType);
        // Execute as getFeature
        return super.getFeature(gft);
    }

    /**
     * Sets the resultSetID
     * 
     * @param resultSetID
     */
    public void setResultSetID(String resultSetId) {
        this.resultSetId = resultSetId;
    }

    /**
     * Helper method that deserializes GetFeature request and updates its last utilization
     * 
     * @param resultSetID
     * @return
     * @throws Exception
     */
    private GetFeatureType getFeature(String resultSetId) throws IOException {
        GetFeatureType feature = null;
        Transaction transaction = new DefaultTransaction("Update");
        try {
            IndexInitializer.READ_WRITE_LOCK.writeLock().lock();
            // Update GetFeature utilization
            DataStore currentDataStore = IndexConfiguration.getCurrentDataStore();
            SimpleFeatureStore store = (SimpleFeatureStore) currentDataStore
                    .getFeatureSource(IndexInitializer.STORE_SCHEMA_NAME);
            store.setTransaction(transaction);
            Filter filter = CQL.toFilter("ID = '" + resultSetId + "'");
            store.modifyFeatures("updated", new Date().getTime(), filter);
            // Retrieve GetFeature from file
            Resource storageResource = IndexConfiguration.getStorageResource();

            // Deserialize KVP parameters and the POST content
            // Gson gson = new GsonBuilder().setPrettyPrinting().create();
            // Reader reader = new FileReader(
            // storageResource.dir().getAbsolutePath() + "\\" + resultSetId + ".feature");
            // Map<String, Map> data = gson.fromJson(reader, Map.class);
            // reader.close();
            /*
             * Kryo kryo = new Kryo(); kryo.setInstantiatorStrategy( new
             * Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
             * kryo.getFieldSerializerConfig().setCopyTransient(false);
             */

            FileInputStream fis = new FileInputStream(
                    storageResource.dir().getAbsolutePath() + "\\" + resultSetId + ".feature");
            BufferedInputStream bis = new BufferedInputStream(fis);

            ObjectInputStream objectinputstream = new ObjectInputStream(bis);
            RequestData data = (RequestData) objectinputstream.readObject();

            /*
             * Input input = new Input(bis);
             * 
             * RequestData data = kryo.readObject(input, RequestData.class);
             */

            objectinputstream.close();

            KvpRequestReader kvpReader = Dispatcher.findKvpRequestReader(GetFeatureType.class);
            Object requestBean = kvpReader.createRequest();
            feature = (GetFeatureType) kvpReader.read(requestBean, data.getKvp(), data.getRawKvp());

            /*
             * Map kvp = data.get("kvp"); Map rawKvp = data.get("rawKvp");
             * 
             * KvpRequestReader kvpReader = Dispatcher.findKvpRequestReader(GetFeatureType.class);
             * Object requestBean = kvpReader.createRequest(); feature = (GetFeatureType)
             * kvpReader.read(requestBean, kvp, rawKvp);
             */

        } catch (Exception t) {
            transaction.rollback();
            throw new RuntimeException("Error on retrive feature", t);
        } finally {
            transaction.close();
            IndexInitializer.READ_WRITE_LOCK.writeLock().unlock();
        }
        return feature;

    }

}
