package org.geoserver.appschema.smart.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.geoserver.appschema.smart.domain.DomainModelVisitorImpl;
import org.geoserver.appschema.smart.domain.entities.DomainModel;
import org.geoserver.appschema.smart.metadata.AttributeMetadata;
import org.geoserver.appschema.smart.metadata.EntityMetadata;
import org.geoserver.appschema.smart.metadata.jdbc.JdbcColumnMetadata;
import org.geoserver.appschema.smart.metadata.jdbc.JdbcForeignKeyColumnMetadata;
import org.geoserver.appschema.smart.metadata.jdbc.JdbcHelper;
import org.geoserver.appschema.smart.metadata.jdbc.JdbcTableMetadata;
import org.geoserver.appschema.smart.metadata.jdbc.constraint.JdbcForeignKeyConstraintMetadata;
import org.geoserver.appschema.smart.metadata.jdbc.constraint.JdbcPrimaryKeyConstraintMetadata;
import org.geoserver.appschema.smart.metadata.jdbc.utils.ResultColumn;
import org.geoserver.appschema.smart.metadata.jdbc.utils.ResultForeignKey;
import org.geoserver.appschema.smart.metadata.jdbc.utils.ResultIndex;
import org.geoserver.appschema.smart.metadata.jdbc.utils.ResultPrimaryKey;
import org.geotools.util.logging.Logging;
import org.w3c.dom.Document;

/**
 * Smart AppSchema Helper for testing purposes.
 *
 * @author Jose Macchi - GeoSolutions
 */
public class SmartAppSchemaTestHelper {

    private static final Logger LOGGER = Logging.getLogger(SmartAppSchemaTestHelper.class);

    public static DatabaseMetaData getConnectionMetaData(String url, String user, String pass)
            throws Exception {
        Connection connection = DriverManager.getConnection(url, user, pass);
        if (connection != null) {
            return connection.getMetaData();
        }
        return null;
    }

    public static <T> void printDomainModel(DomainModel dm) {
        DomainModelVisitorImpl dmv = new LoggerDomainModelVisitor();
        if (dm != null) {
            dm.accept(dmv);
        }
    }

    public static <T> void printObjectsFromList(List<T> list) {
        if (list != null) {
            for (T object : list) {
                LOGGER.log(Level.INFO, object.toString());
            }
        }
    }

    public static void printPrimaryKeys(
            SortedMap<EntityMetadata, JdbcPrimaryKeyConstraintMetadata> pkMap) {
        List<ResultPrimaryKey> pkList = getResultPrimaryKeys(pkMap);
        if (pkList != null) {
            for (ResultPrimaryKey pk : pkList) {
                LOGGER.log(Level.INFO, pk.toString());
            }
        }
    }

    public static void printColumns(SortedMap<JdbcTableMetadata, List<AttributeMetadata>> cMap) {
        List<ResultColumn> cList = getResultColumns(cMap);
        if (cList != null) {
            for (ResultColumn c : cList) {
                LOGGER.log(Level.INFO, c.toString());
            }
        }
    }

    public static void printForeignKeys(
            SortedMap<JdbcForeignKeyConstraintMetadata, Collection<JdbcForeignKeyColumnMetadata>>
                    fkMap,
            SortedMap<EntityMetadata, JdbcPrimaryKeyConstraintMetadata> pkMap,
            SortedMap<String, Collection<String>> uniqueIndexMap) {
        List<ResultForeignKey> resultForeignKeyList =
                getResultForeignKeys(fkMap, pkMap, uniqueIndexMap);
        if (resultForeignKeyList != null) {
            for (ResultForeignKey resultForeignKey : resultForeignKeyList) {
                LOGGER.log(Level.INFO, resultForeignKey.toString());
            }
        }
    }

    public static void printIndexes(SortedMap<String, Collection<String>> indexMap) {
        List<ResultIndex> resultIndices = getResultIndexes(indexMap);
        if (resultIndices != null) {
            for (ResultIndex resultIndex : resultIndices) {
                LOGGER.log(Level.INFO, resultIndex.toString());
            }
        }
    }

    private static List<ResultColumn> getResultColumns(
            SortedMap<JdbcTableMetadata, List<AttributeMetadata>> cMap) {
        if (cMap != null) {
            List<ResultColumn> cList = new ArrayList<ResultColumn>();
            for (Map.Entry<JdbcTableMetadata, List<AttributeMetadata>> cMapEntry :
                    cMap.entrySet()) {
                Iterator<AttributeMetadata> cIterator = cMapEntry.getValue().iterator();
                while (cIterator.hasNext()) {
                    ResultColumn resultC = new ResultColumn((JdbcColumnMetadata) cIterator.next());
                    cList.add(resultC);
                }
            }
            return cList;
        }
        return null;
    }

    private static List<ResultPrimaryKey> getResultPrimaryKeys(
            SortedMap<EntityMetadata, JdbcPrimaryKeyConstraintMetadata> pkMap) {
        if (pkMap != null) {
            List<ResultPrimaryKey> pkList = new ArrayList<ResultPrimaryKey>();
            for (Map.Entry<EntityMetadata, JdbcPrimaryKeyConstraintMetadata> pkMapEntry :
                    pkMap.entrySet()) {
                ResultPrimaryKey resultPk = new ResultPrimaryKey(pkMapEntry.getValue());
                pkList.add(resultPk);
            }
            return pkList;
        }
        return null;
    }

    private static List<ResultForeignKey> getResultForeignKeys(
            SortedMap<JdbcForeignKeyConstraintMetadata, Collection<JdbcForeignKeyColumnMetadata>>
                    fkMap,
            SortedMap<EntityMetadata, JdbcPrimaryKeyConstraintMetadata> pkMap,
            SortedMap<String, Collection<String>> uniqueIndexMap) {
        if (fkMap != null) {
            List<ResultForeignKey> resultForeignKeyList = new ArrayList<ResultForeignKey>();
            for (JdbcForeignKeyConstraintMetadata fkConstraint : fkMap.keySet()) {
                Collection<JdbcForeignKeyColumnMetadata> fkColumnsList = fkMap.get(fkConstraint);

                StringBuilder fkColumnsStr = new StringBuilder();
                StringBuilder pkColumnsStr = new StringBuilder();
                if (fkColumnsList != null && !fkColumnsList.isEmpty()) {
                    for (JdbcForeignKeyColumnMetadata fkColumns : fkColumnsList) {
                        fkColumnsStr.append(fkColumns.getName());
                        fkColumnsStr.append(",");
                        pkColumnsStr.append(fkColumns.getRelatedColumn().getName());
                        pkColumnsStr.append(",");
                    }
                    fkColumnsStr.deleteCharAt(fkColumnsStr.length() - 1);
                    pkColumnsStr.deleteCharAt(pkColumnsStr.length() - 1);
                }

                StringBuilder stringBuilder = new StringBuilder(fkConstraint.getTable().toString());
                stringBuilder.append(fkColumnsStr);
                stringBuilder.append(" -> ");
                stringBuilder.append(fkConstraint.getRelatedTable().toString());
                stringBuilder.append(pkColumnsStr);
                JdbcPrimaryKeyConstraintMetadata primaryKey =
                        JdbcHelper.getInstance()
                                .isPrimaryKey(fkConstraint.getTable(), fkColumnsList, pkMap);
                if (primaryKey != null) {
                    resultForeignKeyList.add(
                            new ResultForeignKey(
                                    "1:1",
                                    fkConstraint.getTable().toString(),
                                    fkColumnsStr.toString(),
                                    fkConstraint.getRelatedTable().toString(),
                                    pkColumnsStr.toString(),
                                    fkConstraint.getName(),
                                    primaryKey.getName()));
                } else {
                    String uniqueIndexConstraint =
                            JdbcHelper.getInstance()
                                    .isUniqueIndex(
                                            fkConstraint.getTable(), fkColumnsList, uniqueIndexMap);
                    if (uniqueIndexConstraint != null) {
                        resultForeignKeyList.add(
                                new ResultForeignKey(
                                        "1:1",
                                        fkConstraint.getTable().toString(),
                                        fkColumnsStr.toString(),
                                        fkConstraint.getRelatedTable().toString(),
                                        pkColumnsStr.toString(),
                                        fkConstraint.getName(),
                                        uniqueIndexConstraint.substring(
                                                fkConstraint.getTable().toString().length() + 3)));
                    } else {
                        resultForeignKeyList.add(
                                new ResultForeignKey(
                                        "N:1",
                                        fkConstraint.getTable().toString(),
                                        fkColumnsStr.toString(),
                                        fkConstraint.getRelatedTable().toString(),
                                        pkColumnsStr.toString(),
                                        fkConstraint.getName(),
                                        null));
                    }
                }
            }
            return resultForeignKeyList;
        }
        return null;
    }

    private static List<ResultIndex> getResultIndexes(
            SortedMap<String, Collection<String>> indexMap) {
        if (indexMap != null) {
            List<ResultIndex> resultIndexes = new ArrayList<ResultIndex>();
            for (String indexConstraint : indexMap.keySet()) {
                String[] indexConstraintAttributes = indexConstraint.split(" - ");

                StringBuilder stringBuilder = new StringBuilder(indexConstraintAttributes[0]);
                Collection<String> indexColumns = indexMap.get(indexConstraint);
                if (indexColumns != null && !indexColumns.isEmpty()) {
                    stringBuilder.append("(");
                    for (String indexColumn : indexColumns) {
                        stringBuilder.append(indexColumn);
                        stringBuilder.append(",");
                    }
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    stringBuilder.append(")");
                }
                resultIndexes.add(
                        new ResultIndex(
                                indexConstraintAttributes[0],
                                stringBuilder.toString(),
                                indexConstraintAttributes[1]));
            }
            return resultIndexes;
        }
        return null;
    }

    public static void printDocument(Document doc, OutputStream out)
            throws IOException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(
                new DOMSource(doc), new StreamResult(new OutputStreamWriter(out, "UTF-8")));
    }

    public static void saveDocumentToFile(Document doc, String pathname)
            throws TransformerFactoryConfigurationError, TransformerConfigurationException,
                    TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transf = transformerFactory.newTransformer();

        transf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transf.setOutputProperty(OutputKeys.INDENT, "yes");
        transf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        DOMSource source = new DOMSource(doc);

        File myFile = new File(pathname);

        StreamResult file = new StreamResult(myFile);

        transf.transform(source, file);
    }

    public static InputStream getResourceAsStream(String fileName) {
        ClassLoader classLoader = SmartAppSchemaTestHelper.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }
    }

    public static File getResourceAsFile(String resourcePath) {
        try {
            InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(resourcePath);
            if (in == null) {
                return null;
            }
            File tempFile = File.createTempFile(String.valueOf(in.hashCode()), ".tmp");
            tempFile.deleteOnExit();
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void printInputStream(InputStream is) {
        try (InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(streamReader)) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveStringToFile(String content, String pathname) throws IOException {
        String str = content;
        BufferedWriter writer = new BufferedWriter(new FileWriter(pathname));
        writer.write(str);
        writer.close();
    }
}
