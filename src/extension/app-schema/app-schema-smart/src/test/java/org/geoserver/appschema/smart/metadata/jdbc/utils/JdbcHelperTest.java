package org.geoserver.appschema.smart.metadata.jdbc.utils;

import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geoserver.appschema.smart.SmartAppSchemaPostgisTestSupport;
import org.geoserver.appschema.smart.metadata.AttributeMetadata;
import org.geoserver.appschema.smart.metadata.EntityMetadata;
import org.geoserver.appschema.smart.metadata.jdbc.JdbcForeignKeyColumnMetadata;
import org.geoserver.appschema.smart.metadata.jdbc.JdbcHelper;
import org.geoserver.appschema.smart.metadata.jdbc.JdbcTableMetadata;
import org.geoserver.appschema.smart.metadata.jdbc.constraint.JdbcForeignKeyConstraintMetadata;
import org.geoserver.appschema.smart.metadata.jdbc.constraint.JdbcPrimaryKeyConstraintMetadata;
import org.geoserver.appschema.smart.utils.SmartAppSchemaTestHelper;
import org.geotools.util.logging.Logging;
import org.junit.Test;

/**
 * Tests related to use of JDBCHelper class.
 *
 * @author Jose Macchi - GeoSolutions
 */
public class JdbcHelperTest extends SmartAppSchemaPostgisTestSupport {

    private static final Logger LOGGER = Logging.getLogger(JdbcHelperTest.class);
    private JdbcHelper JDBC_HELPER = JdbcHelper.getInstance();

    @Test
    public void testMeteoPrimaryKeys() throws Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        List<JdbcTableMetadata> tables = JDBC_HELPER.getSchemaTables(metaData, SCHEMA);
        SortedMap<EntityMetadata, JdbcPrimaryKeyConstraintMetadata> pkMap =
                JDBC_HELPER.getPrimaryKeyColumns(metaData, tables);
        SmartAppSchemaTestHelper.printPrimaryKeys(pkMap);

        assertEquals(true, !pkMap.isEmpty());
        assertEquals(3, pkMap.size());
    }

    @Test
    public void testMeteoSchemaTables() throws Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        List<JdbcTableMetadata> tables = JDBC_HELPER.getSchemaTables(metaData, SCHEMA);
        Iterator<JdbcTableMetadata> it = tables.iterator();
        while (it.hasNext()) {
            JdbcTableMetadata t = it.next();
            LOGGER.log(Level.INFO, t.getCatalog() + " - " + t.getSchema() + " - " + t.getName());
        }

        assertEquals(3, tables.size());
    }

    @Test
    public void testMeteoStationsTablePrimaryKey() throws Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        JdbcTableMetadata table =
                new JdbcTableMetadata(metaData.getConnection(), null, SCHEMA, "meteo_stations");
        List<JdbcTableMetadata> tables = new ArrayList<JdbcTableMetadata>();
        tables.add(table);
        SortedMap<EntityMetadata, JdbcPrimaryKeyConstraintMetadata> pkMap =
                JDBC_HELPER.getPrimaryKeyColumns(metaData, tables);
        SmartAppSchemaTestHelper.printPrimaryKeys(pkMap);

        assertEquals(true, !pkMap.isEmpty());
        assertEquals(1, pkMap.size());
        assertEquals(SCHEMA + ".meteo_stations(id)", pkMap.get(pkMap.firstKey()).toString());
    }

    @Test
    public void testMeteoStationsTableColumns() throws Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        JdbcTableMetadata table =
                new JdbcTableMetadata(metaData.getConnection(), null, SCHEMA, "meteo_stations");
        List<JdbcTableMetadata> tables = new ArrayList<JdbcTableMetadata>();
        tables.add(table);
        SortedMap<JdbcTableMetadata, List<AttributeMetadata>> cMap =
                JDBC_HELPER.getColumns(metaData, tables);
        SmartAppSchemaTestHelper.printColumns(cMap);

        assertEquals(true, !cMap.isEmpty());
        assertEquals(1, cMap.size());
        assertEquals(4, cMap.get(cMap.firstKey()).size());
        assertEquals("id", cMap.get(cMap.firstKey()).get(0).getName());
        assertEquals("code", cMap.get(cMap.firstKey()).get(1).getName());
        assertEquals("common_name", cMap.get(cMap.firstKey()).get(2).getName());
        assertEquals("position", cMap.get(cMap.firstKey()).get(3).getName());
    }

    @Test
    public void testMeteoObservationsTableColumns() throws Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        JdbcTableMetadata table =
                new JdbcTableMetadata(metaData.getConnection(), null, SCHEMA, "meteo_observations");
        List<JdbcTableMetadata> tables = new ArrayList<JdbcTableMetadata>();
        tables.add(table);
        SortedMap<JdbcTableMetadata, List<AttributeMetadata>> cMap =
                JDBC_HELPER.getColumns(metaData, tables);
        SmartAppSchemaTestHelper.printColumns(cMap);

        assertEquals(true, !cMap.isEmpty());
        assertEquals(1, cMap.size());
        assertEquals(5, cMap.get(cMap.firstKey()).size());
        assertEquals("id", cMap.get(cMap.firstKey()).get(0).getName());
        assertEquals("parameter_id", cMap.get(cMap.firstKey()).get(1).getName());
        assertEquals("station_id", cMap.get(cMap.firstKey()).get(2).getName());
        assertEquals("time", cMap.get(cMap.firstKey()).get(3).getName());
        assertEquals("value", cMap.get(cMap.firstKey()).get(4).getName());
    }

    @Test
    public void testMeteoSchemaForeignKeys() throws Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        List<JdbcTableMetadata> tables = JDBC_HELPER.getSchemaTables(metaData, SCHEMA);
        SortedMap<JdbcForeignKeyConstraintMetadata, Collection<JdbcForeignKeyColumnMetadata>>
                fkMap = JDBC_HELPER.getForeignKeys(metaData, tables);
        SortedMap<EntityMetadata, JdbcPrimaryKeyConstraintMetadata> pkMap =
                JDBC_HELPER.getPrimaryKeyColumns(metaData, tables);
        SortedMap<String, Collection<String>> uniqueIndexMultimap =
                JDBC_HELPER.getIndexColumns(metaData, tables, true, true);
        SmartAppSchemaTestHelper.printForeignKeys(fkMap, pkMap, uniqueIndexMultimap);

        assertEquals(true, !fkMap.isEmpty());
        assertEquals(2, fkMap.size());
    }

    @Test
    public void testMeteoObservationsTableForeignKeys() throws Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        JdbcTableMetadata table =
                new JdbcTableMetadata(metaData.getConnection(), null, SCHEMA, "meteo_observations");
        SortedMap<JdbcForeignKeyConstraintMetadata, Collection<JdbcForeignKeyColumnMetadata>>
                fkMultimap = JDBC_HELPER.getForeignKeysByTable(metaData, table);
        JdbcPrimaryKeyConstraintMetadata primaryKey =
                JDBC_HELPER.getPrimaryKeyColumnsByTable(metaData, table);
        SortedMap<EntityMetadata, JdbcPrimaryKeyConstraintMetadata> pkMap =
                new TreeMap<EntityMetadata, JdbcPrimaryKeyConstraintMetadata>();
        pkMap.put(table, primaryKey);
        SortedMap<String, Collection<String>> uniqueIndexMultimap =
                JDBC_HELPER.getIndexesByTable(metaData, table, true, true);
        SmartAppSchemaTestHelper.printForeignKeys(fkMultimap, pkMap, uniqueIndexMultimap);

        assertEquals(true, !fkMultimap.isEmpty());
        assertEquals(2, fkMultimap.size());
    }

    @Test
    public void testMeteoStationsTableForeignKeys() throws Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        JdbcTableMetadata table =
                new JdbcTableMetadata(metaData.getConnection(), null, SCHEMA, "meteo_stations");
        SortedMap<JdbcForeignKeyConstraintMetadata, Collection<JdbcForeignKeyColumnMetadata>>
                fkMultimap = JDBC_HELPER.getForeignKeysByTable(metaData, table);
        JdbcPrimaryKeyConstraintMetadata primaryKey =
                JDBC_HELPER.getPrimaryKeyColumnsByTable(metaData, table);
        SortedMap<EntityMetadata, JdbcPrimaryKeyConstraintMetadata> pkMap =
                new TreeMap<EntityMetadata, JdbcPrimaryKeyConstraintMetadata>();
        pkMap.put(table, primaryKey);
        SortedMap<String, Collection<String>> uniqueIndexMultimap =
                JDBC_HELPER.getIndexesByTable(metaData, table, true, true);
        SmartAppSchemaTestHelper.printForeignKeys(fkMultimap, pkMap, uniqueIndexMultimap);

        // no fk at meteo_stations
        assertEquals(null, fkMultimap);
    }

    @Test
    public void testIndexes() throws Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        List<JdbcTableMetadata> tables = JDBC_HELPER.getSchemaTables(metaData, SCHEMA);
        SortedMap<String, Collection<String>> uniqueIndexMultimap =
                JDBC_HELPER.getIndexColumns(metaData, tables, false, true);
        SmartAppSchemaTestHelper.printIndexes(uniqueIndexMultimap);

        assertEquals(true, !uniqueIndexMultimap.isEmpty());
        assertEquals(3, uniqueIndexMultimap.size());
        assertEquals(
                true,
                uniqueIndexMultimap.containsKey(
                        SCHEMA + ".meteo_observations - meteo_observations_pkey"));
        assertEquals(
                true,
                uniqueIndexMultimap.containsKey(
                        SCHEMA + ".meteo_parameters - meteo_parameters_pkey"));
        assertEquals(
                true,
                uniqueIndexMultimap.containsKey(SCHEMA + ".meteo_stations - meteo_stations_pkey"));
    }

    @Test
    public void testMeteoStationTableIndexes() throws Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        JdbcTableMetadata table =
                new JdbcTableMetadata(metaData.getConnection(), null, SCHEMA, "meteo_stations");
        SortedMap<String, Collection<String>> uniqueIndexMultimap =
                JDBC_HELPER.getIndexesByTable(metaData, table, false, true);
        SmartAppSchemaTestHelper.printIndexes(uniqueIndexMultimap);

        assertEquals(true, !uniqueIndexMultimap.isEmpty());
        assertEquals(1, uniqueIndexMultimap.size());
        assertEquals(
                true,
                uniqueIndexMultimap.containsKey(SCHEMA + ".meteo_stations - meteo_stations_pkey"));
    }

    @Test
    public void testUniqueIndexes() throws Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        List<JdbcTableMetadata> tables = JDBC_HELPER.getSchemaTables(metaData, SCHEMA);
        SortedMap<String, Collection<String>> uniqueIndexMultimap =
                JDBC_HELPER.getIndexColumns(metaData, tables, true, true);
        SmartAppSchemaTestHelper.printIndexes(uniqueIndexMultimap);

        assertEquals(true, !uniqueIndexMultimap.isEmpty());
        assertEquals(3, uniqueIndexMultimap.size());
        assertEquals(
                true,
                uniqueIndexMultimap.containsKey(
                        SCHEMA + ".meteo_observations - meteo_observations_pkey"));
        assertEquals(
                true,
                uniqueIndexMultimap.containsKey(
                        SCHEMA + ".meteo_parameters - meteo_parameters_pkey"));
        assertEquals(
                true,
                uniqueIndexMultimap.containsKey(SCHEMA + ".meteo_stations - meteo_stations_pkey"));
    }

    @Test
    public void testMeteoStationsTableUniqueIndexes() throws Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        JdbcTableMetadata table =
                new JdbcTableMetadata(metaData.getConnection(), null, SCHEMA, "meteo_stations");
        SortedMap<String, Collection<String>> uniqueIndexMultimap =
                JDBC_HELPER.getIndexesByTable(metaData, table, true, true);
        SmartAppSchemaTestHelper.printIndexes(uniqueIndexMultimap);

        assertEquals(true, !uniqueIndexMultimap.isEmpty());
        assertEquals(1, uniqueIndexMultimap.size());
        assertEquals(
                true,
                uniqueIndexMultimap.containsKey(SCHEMA + ".meteo_stations - meteo_stations_pkey"));
    }
}
